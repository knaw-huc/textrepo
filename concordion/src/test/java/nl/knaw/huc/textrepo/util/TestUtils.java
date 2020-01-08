package nl.knaw.huc.textrepo.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.textrepo.Config.FILE_TYPE;

public class TestUtils {

  public static byte[] getResourceFileBits(String resourcePath) throws IOException {
    return IOUtils.toByteArray(TestUtils.class.getClassLoader().getResourceAsStream(resourcePath));
  }

  static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

  public static String isValidUuidMsg(String fileId) {
    try {
      UUID.fromString(fileId);
      return "valid UUID";
    } catch (Exception e) {
      return "invalid UUID: " + e.getMessage();
    }
  }

  public static boolean isValidUuid(String fileId) {
    try {
      UUID.fromString(fileId);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static String getFileId(String location) {
    var pattern = Pattern.compile(".*\\/files\\/(.*)\\/latest");
    var matcher = pattern.matcher(location);
    if (matcher.matches()) {
      return matcher.group(1);
    }
    throw new IllegalStateException(format("No file id in location [%s]", location));
  }

  public static Entity<FormDataMultiPart> getMultiPartEntity(FormDataMultiPart multiPart) {
    return entity(multiPart, multiPart.getMediaType());
  }

  public static Optional<String> getLocation(Response response) {
    return Optional.ofNullable(response.getHeaderString("Location"));
  }

  public static Response postFileWithFilename(
      Client client, URL filesEndpoint, String filename, byte[] content
  ) {
    logger.info("Posting file [{}] to [{}]", filename, filesEndpoint);
    var contentDisposition = FormDataContentDisposition
        .name("contents")
        .fileName(filename)
        .size(content.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .field("type", FILE_TYPE)
        .bodyPart(new FormDataBodyPart(
            contentDisposition,
            content,
            APPLICATION_OCTET_STREAM_TYPE)
        );

    final var request = client
        .register(MultiPartFeature.class)
        .target(filesEndpoint.toString())
        .request();

    final var entity = entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }

  public static Response putFileWithFilename(
      Client client,
      String putFileContentsUrl,
      String filename,
      byte[] content
  ) {
    var contentDisposition = FormDataContentDisposition
        .name("contents")
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
        .target(putFileContentsUrl)
        .request();

    final var entity = entity(multiPart, multiPart.getMediaType());

    return request.put(entity);
  }

  public static String replace(String url, String placeholder, String value) {
    var toReplace = Map.of(placeholder, value);
    return StringSubstitutor.replace(url, toReplace, "{", "}");
  }

  public static void sleepMs(int timeout) {
    try {
      MILLISECONDS.sleep(timeout);
    } catch (InterruptedException ex) {
      throw new RuntimeException("Could not sleep", ex);
    }
  }

}
