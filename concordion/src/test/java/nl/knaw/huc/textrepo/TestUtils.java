package nl.knaw.huc.textrepo;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;

public class TestUtils {

  public static byte[] getResourceFileBits(String resourcePath) throws IOException {
    return IOUtils.toByteArray(TestUtils.class.getClassLoader().getResourceAsStream(resourcePath));
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

  public static Response postFileWithFilename(
      Client client, URL documents, String filename, byte[] content
  ) {
    var contentDisposition = FormDataContentDisposition
        .name("file")
        .fileName(filename)
        .size(content.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(
            contentDisposition,
            content,
            APPLICATION_OCTET_STREAM_TYPE)
        );

    final var request = client
        .register(MultiPartFeature.class)
        .target(documents.toString())
        .request();

    final var entity = Entity.entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }

  public static Response putFileWithFilename(
      Client client,
      String putDocumentFileUrl,
      String filename,
      byte[] content
  ) {
    var contentDisposition = FormDataContentDisposition
        .name("file")
        .fileName(filename)
        .size(content.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(
            contentDisposition,
            content,
            APPLICATION_OCTET_STREAM_TYPE)
        );

    final var request = client
        .register(MultiPartFeature.class)
        .target(putDocumentFileUrl)
        .request();

    final var entity = Entity.entity(multiPart, multiPart.getMediaType());

    return request.put(entity);
  }

  public static String replace(String url, String placeholder, String value) {
    var toReplace = Map.of(placeholder, value);
    return StringSubstitutor.replace(url, toReplace, "{", "}");
  }


}
