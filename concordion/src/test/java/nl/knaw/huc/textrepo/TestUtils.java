package nl.knaw.huc.textrepo;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TestUtils {
  public static byte[] getResourceFileBits(String resourcePath) throws IOException {
    return IOUtils.toByteArray(TestUtils.class.getClassLoader().getResourceAsStream(resourcePath));
  }

  public static String getResourceFileString(String resourcePath) throws IOException {
    return IOUtils.toString(TestUtils.class.getClassLoader().getResourceAsStream(resourcePath), UTF_8);
  }
  public static String isValidUUID(String documentId) {
    try {
      UUID.fromString(documentId);
      return "valid UUID";
    } catch (Exception e) {
      return "invalid UUID: " + e.getMessage();
    }
  }

  public static String getDocumentId(String location) {
    return location.substring(location.lastIndexOf('/') + 1);
  }

  public static Entity<FormDataMultiPart> getMultiPartEntity(FormDataMultiPart multiPart) {
    return Entity.entity(multiPart, multiPart.getMediaType());
  }

  public static Optional<String> getLocation(Response response) {
    return Optional.ofNullable(response.getHeaderString("Location"));
  }


}
