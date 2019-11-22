package nl.knaw.huc.resources;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

public class ResourceUtils {
  public static byte[] readContent(InputStream uploadedInputStream) {
    if (uploadedInputStream == null) {
      throw new BadRequestException("File is missing");
    }

    try {
      return uploadedInputStream.readAllBytes();
    } catch (IOException e) {
      throw new BadRequestException("Could not read input stream of posted file", e);
    }
  }

  public static URI locationOf(UUID uuid) {
    return UriBuilder
        .fromResource(FilesResource.class)
        .path("{uuid}")
        .build(uuid);
  }
}
