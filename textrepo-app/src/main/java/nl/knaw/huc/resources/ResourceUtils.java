package nl.knaw.huc.resources;

import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.BadRequestException;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.exceptions.PayloadTooLargeException;
import nl.knaw.huc.helpers.digest.DigestComputingInputStream;
import nl.knaw.huc.helpers.gzip.GzipCompressingInputStream;
import nl.knaw.huc.helpers.gzip.GzipHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceUtils {
  private static final Logger log = LoggerFactory.getLogger(ResourceUtils.class);

  public static Contents readContents(InputStream inputStream) {
    if (inputStream == null) {
      throw new BadRequestException("File is missing");
    }

    try {
      // decompress if necessary, compute digest on (decompressed) content, then compress for
      // storage
      final var originalContentStream = GzipHelper.decompressIfNeeded(inputStream);
      final var digestComputingStream = new DigestComputingInputStream(originalContentStream);
      final var compressedInputStream = new GzipCompressingInputStream(digestComputingStream);

      // hog memory to get all the (compressed) input bytes into 'contents'
      final var bytes = compressedInputStream.readAllBytes();
      final var sha224 = digestComputingStream.digestAsHex();
      final var contents = new Contents(sha224, bytes);
      log.debug("Contents prepared, size={}: {}", bytes.length, contents);
      return contents;
    } catch (IOException e) {
      throw new BadRequestException("Could not read posted file", e);
    }
  }

  private static class InputStreamLimiter extends InputStream {
    private final InputStream delegate;
    private final int maxAllowedSize;
    private int numRead;

    private InputStreamLimiter(InputStream delegate, int maxAllowedSize) {
      this.delegate = delegate;
      this.maxAllowedSize = maxAllowedSize;
    }

    @Override
    public int read() throws IOException {
      final int c = delegate.read();

      if (c == -1) { // No limit on reading EOF
        return -1;
      }

      if (++numRead > maxAllowedSize) {
        throw new PayloadTooLargeException("max. allowed size: " + maxAllowedSize);
      }

      return c;
    }
  }

}
