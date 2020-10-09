package nl.knaw.huc.helpers;

import nl.knaw.huc.core.Contents;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_ENCODING;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

public class ContentsHelper {
  public static Response.ResponseBuilder getContentsAsAttachment(Contents contents, int decompressLimit) {
    final Response.ResponseBuilder builder;
    if (contents.canDecompressInMemory(decompressLimit)) {
      builder = Response.ok(contents.decompress(), APPLICATION_OCTET_STREAM);
    } else {
      builder = Response.ok(contents.getContents(), APPLICATION_OCTET_STREAM)
                        .header(CONTENT_ENCODING, "gzip");
    }
    return builder
        .header(CONTENT_DISPOSITION, "attachment;");
  }
}
