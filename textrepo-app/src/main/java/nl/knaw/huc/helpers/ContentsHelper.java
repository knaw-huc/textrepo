package nl.knaw.huc.helpers;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_ENCODING;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.helpers.gzip.GzipCompressingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentsHelper {
  private static final Logger log = LoggerFactory.getLogger(ContentsHelper.class);

  private static final String GZIP_ENCODED = "gzip";

  private final int contentDecompressionLimit;

  public ContentsHelper(int contentDecompressionLimit) {
    this.contentDecompressionLimit = contentDecompressionLimit;
  }

  public ResponseBuilder asAttachment(@Nonnull Contents contents,
                                      @Nullable String acceptEncoding) {
    final boolean compressionRequested = compressionRequested(acceptEncoding);

    final ResponseBuilder builder;
    if (compressionRequested || !contents.canDecompressInMemory(contentDecompressionLimit)) {
      builder = Response.ok(contents.getContents())
                        .header(CONTENT_ENCODING, GZIP_ENCODED);
    } else {
      builder = Response.ok(contents.decompress());
    }

    return builder.header(CONTENT_DISPOSITION, "attachment;");
  }

  public ResponseBuilder asAttachment(@Nonnull String contents, @Nullable String acceptEncoding) {
    if (compressionRequested(acceptEncoding)) {
      try {
        return Response.ok(compress(contents))
                       .header(CONTENT_DISPOSITION, "attachment;")
                       .header(CONTENT_ENCODING, GZIP_ENCODED);
      } catch (IOException err) {
        log.warn("Failed to compress: {}", err.getMessage());
      }
    }

    return Response.ok(contents)
                   .header(CONTENT_DISPOSITION, "attachment;");
  }

  private InputStream compress(@Nonnull String contents) throws IOException {
    return new GzipCompressingInputStream(new ByteArrayInputStream(contents.getBytes(UTF_8)));
  }

  private boolean compressionRequested(@Nullable String acceptEncoding) {
    final var compressionRequested =
        acceptEncoding != null && acceptEncoding.contains(GZIP_ENCODED);

    if (log.isDebugEnabled()) {
      if (acceptEncoding != null && !compressionRequested) {
        log.debug("Accept-Encoding: ignoring unsupported compression method: {}", acceptEncoding);
      }
    }

    return compressionRequested;
  }
}
