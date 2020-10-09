package nl.knaw.huc.textrepo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StringSubstitutor;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.Config.TEXT_TYPE;

public class TestUtils {

  static final Logger log = LoggerFactory.getLogger(TestUtils.class);
  static final ObjectMapper mapper = new ObjectMapper();
  private static Client client = JerseyClientBuilder.newClient();

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
    return getId(location, pattern);
  }

  private static String getId(String location, Pattern pattern) {
    var matcher = pattern.matcher(location);
    if (matcher.matches()) {
      return matcher.group(1);
    }
    throw new IllegalStateException(format("No file id in location [%s]", location));
  }

  public static Optional<String> getLocation(Response response) {
    return Optional.ofNullable(response.getHeaderString("Location"));
  }

  public static Response postFileWithFilename(
      Client client, URL filesEndpoint, String filename, byte[] content
  ) {
    log.info("Posting file [{}] to [{}]", filename, filesEndpoint);
    var contentDisposition = FormDataContentDisposition
        .name("contents")
        .fileName(filename)
        .size(content.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .field("type", TEXT_TYPE)
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

  public static void sleepMs(int timeout) {
    try {
      MILLISECONDS.sleep(timeout);
    } catch (InterruptedException ex) {
      throw new RuntimeException("Could not sleep", ex);
    }
  }

  public static String getStatus(Response response) {
    return response.getStatus() + " " + response.getStatusInfo();
  }

  /**
   * Return formatted json within pre-tags
   */
  public static String asPrettyJson(String string) {
    var result = "";
    try {
      var json = mapper.readValue(string, Object.class);
      result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException(format("Could not pretty print json string: [%s]", string), ex);
    }
    return asCodeBlock(result);
  }

  public static String asCodeBlock(String result) {
    return "<pre>" + result + "</pre>";
  }

  public static URI replaceUrlParams(Object endpoint, Object... params) {
    return UriBuilder.fromPath(HOST + endpoint.toString()).build(params);
  }

  public static URI replaceInUrlAndQueryParams(Object endpoint, Map<String, String> params) {
    var url = HOST + endpoint;
    for (var p : params.entrySet()) {
      url = url.replace(p.getKey(), p.getValue());
    }
    try {
      return new URI(url);
    } catch (URISyntaxException ex) {
      throw new RuntimeException("Could not create URI with query params from: " + url, ex);
    }
  }

}
