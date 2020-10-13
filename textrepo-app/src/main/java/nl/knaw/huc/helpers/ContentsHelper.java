package nl.knaw.huc.helpers;

import nl.knaw.huc.core.Contents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_ENCODING;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

public class ContentsHelper {
  private static final Logger log = LoggerFactory.getLogger(ContentsHelper.class);

  private final int contentDecompressionLimit;

  public ContentsHelper(int contentDecompressionLimit) {
    this.contentDecompressionLimit = contentDecompressionLimit;
    log.debug("contentDecompressionLimit={}", contentDecompressionLimit);
  }

  public Response.ResponseBuilder getContentsAsAttachment(Contents contents) {
    final Response.ResponseBuilder builder;
    if (contents.canDecompressInMemory(contentDecompressionLimit)) {
      builder = Response.ok(contents.decompress(), APPLICATION_OCTET_STREAM);
    } else {
      builder = Response.ok(contents.getContents(), APPLICATION_OCTET_STREAM)
                        .header(CONTENT_ENCODING, "gzip");
    }
    return builder
        .header(CONTENT_DISPOSITION, "attachment;");
  }
}
