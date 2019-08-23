package nl.knaw.huc.resources;

import nl.knaw.huc.api.Version;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class ResourceUtils {
  static byte[] readContent(InputStream uploadedInputStream) {
    if (uploadedInputStream == null) {
      throw new BadRequestException("File is missing");
    }

    try {
      return uploadedInputStream.readAllBytes();
    } catch (IOException e) {
      throw new BadRequestException("Could not read input stream of posted file", e);
    }
  }

  public static URI locationOf(Version version) {
    return UriBuilder
        .fromResource(DocumentsResource.class)
        .path("{uuid}")
        .build(version.getDocumentUuid());
  }
}
