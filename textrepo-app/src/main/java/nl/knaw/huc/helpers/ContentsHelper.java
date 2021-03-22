package nl.knaw.huc.helpers;

import nl.knaw.huc.core.Contents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.Optional;

import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_ENCODING;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

public class ContentsHelper {
  private static final Logger log = LoggerFactory.getLogger(ContentsHelper.class);

  private static final String GZIP_ENCODED = "gzip";

  private final int contentDecompressionLimit;

  public ContentsHelper(int contentDecompressionLimit) {
    this.contentDecompressionLimit = contentDecompressionLimit;
  }

  public ResponseBuilder asAttachment(@Nonnull Contents contents, @Nullable String acceptEncoding) {
    final boolean compressionRequested = Optional.ofNullable(acceptEncoding)
                                                 .map(str -> str.contains(GZIP_ENCODED))
                                                 .orElse(false);

    if (log.isDebugEnabled()) {
      if (acceptEncoding != null && !compressionRequested) {
        log.debug("Accept-Encoding: ignoring unsupported compression method: {}", acceptEncoding);
      }
    }

    final ResponseBuilder builder;
    if (compressionRequested || !contents.canDecompressInMemory(contentDecompressionLimit)) {
      builder = Response.ok(contents.getContents(), APPLICATION_OCTET_STREAM)
                        .header(CONTENT_ENCODING, GZIP_ENCODED);
    } else {
      builder = Response.ok(contents.decompress(), APPLICATION_OCTET_STREAM);
    }

    return builder.header(CONTENT_DISPOSITION, "attachment;");
  }
}
