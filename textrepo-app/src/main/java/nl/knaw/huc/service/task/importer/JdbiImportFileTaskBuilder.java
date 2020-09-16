package nl.knaw.huc.service.task.importer;

import com.google.common.io.CountingInputStream;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.LargeObjectsDao;
import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.HaveDocumentByExternalId;
import nl.knaw.huc.service.task.HaveFileForDocumentByType;
import nl.knaw.huc.service.task.InTransactionProvider;
import nl.knaw.huc.service.task.SetCurrentFileContents;
import nl.knaw.huc.service.task.SetFileProvenance;
import nl.knaw.huc.service.task.Task;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.zip.GZIPInputStream.GZIP_MAGIC;
import static nl.knaw.huc.core.Contents.fromBytes;

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
      MessageDigest digest = DigestUtils.getDigest(MessageDigestAlgorithms.SHA_224);
      // TODO: digest chunks as we read, decompress if GZIPed, to compute hash of the (uncompressed) input.

      return jdbi.inTransaction(th -> {
        var lob = th.attach(LargeObjectsDao.class);
        var cis = new CountingInputStream(inputStream);
        var pbis = new PushbackInputStream(cis, 2);
        var gzipCompressed = false;
        try {
          byte[] magic = new byte[2];
          int nread = pbis.read(magic);
          if (nread > 0) {
            pbis.unread(magic, 0, nread);
            if (magic[0] == (byte) GZIP_MAGIC && magic[1] == (byte) (GZIP_MAGIC >> 8)) {
              log.debug("GZIP'ed INPUT DETECTED");
              gzipCompressed = true;
            }
          }
        } catch (IOException e) {
          throw new BadRequestException("Could not read input stream of posted file", e);
        }

        var id = lob.insert(pbis);
        log.debug("Inserted {} into large_objects, gzip={}, size={}", id, gzipCompressed, cis.getCount());
        final Document doc = documentFinder.executeIn(th);
        final var file = new HaveFileForDocumentByType(fileIdGenerator, doc, typeName).executeIn(th);
        new SetFileProvenance(file, filename).executeIn(th);
        final var contents = fromBytes("lorem ipsum".getBytes(UTF_8));//fromBytes(inputStream.readAllBytes());
        return new SetCurrentFileContents(versionIdGenerator, file, contents).executeIn(th);
      });
    }
  }

}
