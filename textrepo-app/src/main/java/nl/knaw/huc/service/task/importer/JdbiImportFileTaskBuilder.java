package nl.knaw.huc.service.task.importer;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.HaveDocumentByExternalId;
import nl.knaw.huc.service.task.HaveFileForDocumentByType;
import nl.knaw.huc.service.task.InTransactionProvider;
import nl.knaw.huc.service.task.SetCurrentFileContents;
import nl.knaw.huc.service.task.SetFileProvenance;
import nl.knaw.huc.service.task.Task;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.BadRequestException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA_224;

public class JdbiImportFileTaskBuilder implements ImportFileTaskBuilder {
  private static final Logger log = LoggerFactory.getLogger(JdbiImportFileTaskBuilder.class);

  private final Jdbi jdbi;
  private final Supplier<UUID> documentIdGenerator;
  private final Supplier<UUID> fileIdGenerator;
  private final Supplier<UUID> versionIdGenerator;

  private String externalId;
  private String typeName;
  private String filename;
  private byte[] contents;
  private boolean allowNewDocument;
  private InputStream inputStream;

  public JdbiImportFileTaskBuilder(Jdbi jdbi, Supplier<UUID> idGenerator) {
    this.jdbi = requireNonNull(jdbi);
    this.documentIdGenerator = requireNonNull(idGenerator);
    this.fileIdGenerator = requireNonNull(idGenerator);
    this.versionIdGenerator = requireNonNull(idGenerator);
  }

  @Override
  public ImportFileTaskBuilder allowNewDocument(boolean allowNewDocument) {
    this.allowNewDocument = allowNewDocument;
    return this;
  }

  @Override
  public ImportFileTaskBuilder forExternalId(String externalId) {
    this.externalId = requireNonNull(externalId);
    return this;
  }

  @Override
  public ImportFileTaskBuilder withTypeName(String typeName) {
    this.typeName = requireNonNull(typeName);
    return this;
  }

  @Override
  public ImportFileTaskBuilder forFilename(String name) {
    this.filename = requireNonNull(name);
    return this;
  }

  @Override
  public ImportFileTaskBuilder withContents(byte[] contents) {
    this.contents = requireNonNull(contents);
    return this;
  }

  @Override
  public ImportFileTaskBuilder withInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
    return this;
  }

  @Override
  public Task<Version> build() {
    final InTransactionProvider<Document> documentFinder;
    if (allowNewDocument) {
      documentFinder = new HaveDocumentByExternalId(documentIdGenerator, externalId);
    } else {
      documentFinder = new FindDocumentByExternalId(externalId);
    }
    return new JdbiImportDocumentTask(documentFinder, typeName, filename, inputStream);
  }

  private class JdbiImportDocumentTask implements Task<Version> {
    private final InTransactionProvider<Document> documentFinder;
    private final String typeName;
    private final String filename;
    private final InputStream inputStream;

    private JdbiImportDocumentTask(InTransactionProvider<Document> documentFinder,
                                   String typeName, String filename, InputStream inputStream) {
      this.documentFinder = documentFinder;
      this.typeName = typeName;
      this.filename = filename;
      this.inputStream = inputStream;
    }

    @Override
    public Version run() {
      return jdbi.inTransaction(th -> {
        var originalContent = decompressIfNeeded(inputStream);
        var digestComputingStream = new DigestComputingInputStream(originalContent, SHA_224);
        var compressedInputStream = compressInput(digestComputingStream);
        final var bytes = readAllBytes(compressedInputStream);
        final var sha224 = digestComputingStream.digestAsHex();
        final var contents = new Contents(sha224, bytes);
        log.debug("Contents prepared: length={}; sha224={}", bytes.length, sha224);

        final Document doc = documentFinder.executeIn(th);
        final var file = new HaveFileForDocumentByType(fileIdGenerator, doc, typeName).executeIn(th);
        new SetFileProvenance(file, filename).executeIn(th);
        return new SetCurrentFileContents(versionIdGenerator, file, contents).executeIn(th);
      });
    }

    private byte[] readAllBytes(InputStream inputStream) {
      try {
        return inputStream.readAllBytes();
      } catch (IOException e) {
        throw new BadRequestException("Could not read all bytes of posted file", e);
      }
    }

    private InputStream compressInput(InputStream uncompressed) {
      try {
        return new GzipCompressingInputStream(uncompressed);
      } catch (IOException e) {
        throw new BadRequestException("Unable to compress input", e);
      }
    }

    private InputStream decompressIfNeeded(InputStream inputStream) {
      GzipDetectingInputStream is = new GzipDetectingInputStream(inputStream);
      if (is.isGzipCompressed()) {
        try {
          return new GZIPInputStream(is);
        } catch (IOException e) {
          throw new BadRequestException("Unable to decompress gzip compressed input", e);
        }
      }
      return is;
    }

    class DigestComputingInputStream extends FilterInputStream {
      private final MessageDigest digest;

      protected DigestComputingInputStream(InputStream in) {
        this(in, SHA_224);
      }

      protected DigestComputingInputStream(InputStream in, String algorithm) {
        super(in);
        this.digest = DigestUtils.getDigest(algorithm);
      }

      @Override
      public int read() {
        try {
          final int nread = in.read();
          if (nread > 0) {
            digest.update((byte) nread);
          }
          return nread;
        } catch (IOException e) {
          throw new BadRequestException("Could not compute digest of posted file (1)", e);
        }
      }

      @Override
      public int read(@Nonnull byte[] data, int off, int len) {
        try {
          final var nread = in.read(data, off, len);
          if (nread > 0) {
            digest.update(data, off, nread);
          }
          return nread;
        } catch (IOException e) {
          throw new BadRequestException("Could not compute digest of posted file (3)", e);
        }
      }

      public String digestAsHex() {
        return Hex.encodeHexString(digest.digest());
      }
    }

    class GzipDetectingInputStream extends PushbackInputStream {
      private static final byte GZIP_MAGIC_0 = (byte) GZIPInputStream.GZIP_MAGIC;
      private static final byte GZIP_MAGIC_1 = (byte) (GZIPInputStream.GZIP_MAGIC >> 8);

      private boolean isGzipCompressed = false;

      protected GzipDetectingInputStream(InputStream in) {
        super(in, 2);

        try {
          byte[] magic = new byte[2];
          int nread = read(magic);
          if (nread > 0) {
            unread(magic, 0, nread);
            isGzipCompressed = (magic[0] == GZIP_MAGIC_0 && magic[1] == GZIP_MAGIC_1);
          }
        } catch (IOException e) {
          throw new BadRequestException("Could not read input stream of posted file", e);
        }
      }

      public boolean isGzipCompressed() {
        return isGzipCompressed;
      }
    }

    public class GzipCompressingInputStream extends InputStream {
      private final InputStream in;
      private final GZIPOutputStream gz;

      private byte[] buf = new byte[8192];
      private final byte[] readBuf = new byte[8192];
      private int read = 0;
      private int write = 0;

      public GzipCompressingInputStream(InputStream in) throws IOException {
        this.in = in;
        // grow the array if we don't have enough space to fulfill the incoming data
        final OutputStream delegate = new OutputStream() {

          private void growBufferIfNeeded(int len) {
            if ((write + len) >= buf.length) {
              // grow the array if we don't have enough space to fulfill the incoming data
              byte[] newbuf = new byte[(buf.length + len) * 2];
              System.arraycopy(buf, 0, newbuf, 0, buf.length);
              buf = newbuf;
            }
          }

          @Override
          public void write(@Nonnull byte[] data, int off, int len) {
            growBufferIfNeeded(len);
            System.arraycopy(data, off, buf, write, len);
            write += len;
          }

          @Override
          public void write(int data) {
            growBufferIfNeeded(1);
            buf[write++] = (byte) data;
          }
        };
        this.gz = new GZIPOutputStream(delegate);
      }

      @Override
      public int read(@Nonnull byte[] data, int off, int len) throws IOException {
        compressStream();
        int numBytes = Math.min(len, write - read);
        if (numBytes > 0) {
          System.arraycopy(buf, read, data, off, numBytes);
          read += numBytes;
        } else if (len > 0) {
          // if bytes were requested, but we have none, then we're at the end of the stream
          return -1;
        }
        return numBytes;
      }

      @Override
      public int read() throws IOException {
        compressStream();
        if (write == 0) {
          // write should not be 0 if we were able to get data from compress stream, must mean we're at the end
          return -1;
        } else {
          // reading a single byte
          return buf[read++] & 0xFF;
        }
      }

      private void compressStream() throws IOException {
        // if the reader has caught up with the writer, then zero the positions out
        if (read == write) {
          read = 0;
          write = 0;
        }

        while (write == 0) {
          // feed the gzip stream data until it spits out a block
          int val = in.read(readBuf);
          if (val == -1) {
            // nothing left to do, we've hit the end of the stream. finalize and break out
            gz.close();
            break;
          } else if (val > 0) {
            gz.write(readBuf, 0, val);
          }
        }
      }
    }
  }
}
