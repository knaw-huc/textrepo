package nl.knaw.huc.textrepo;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.textrepo.AbstractConcordionTest.ES_HOST;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
    return location.substring(location.lastIndexOf('/') + 1);
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

  public static URL indexToUrl(String index) {
    try {
      return new URL(ES_HOST + "/" + index);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(format("Could not create url from index [%s]", index));
    }
  }

  public static void refreshIndex(Client client, String index) {
    var uri = indexToUrl(index) + "/_refresh";
    var refreshRequest = client
        .register(MultiPartFeature.class)
        .target(uri)
        .request()
        .post(entity("", APPLICATION_JSON_TYPE));
    assertThat(refreshRequest.getStatus()).isEqualTo(200);
    // wait a bit until refreshed:
    sleepMs(100);
  }

  public static void sleepMs(int timeout) {
    try {
      MILLISECONDS.sleep(timeout);
    } catch (InterruptedException ex) {
      throw new RuntimeException("Could not sleep", ex);
    }
  }

}
