package nl.knaw.huc.resources;

import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.io.InputStream;

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
}
