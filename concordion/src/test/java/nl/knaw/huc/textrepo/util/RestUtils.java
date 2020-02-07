package nl.knaw.huc.textrepo.util;

import com.jayway.jsonpath.JsonPath;
import nl.knaw.huc.textrepo.rest.TestRestVersions;
import org.apache.commons.lang3.RandomStringUtils;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Client;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.Config.TYPES_URL;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class RestUtils {

  private static Client client = JerseyClientBuilder.newClient();

  /**
   * @return document uuid
   */
  public static String createDocument() {
    final var response = client
        .target(HOST + "/rest/documents")
        .request()
        .post(entity("{\"externalId\":  \"dummy-" + randomAlphabetic(5) + "\"}", APPLICATION_JSON_TYPE));

    var body = response.readEntity(String.class);
    return JsonPath.parse(body).read("$.id");
  }

  public static int createType(String type, String mimetype) {
    var response = client
        .target(TYPES_URL)
        .request()
        .post(json(format("{\"name\": \"%s\",\"mimetype\": \"%s\"}", type, mimetype)));
    var body = response.readEntity(String.class);
    return JsonPath.parse(body).read("$.id");
  }

  public static String createFile(String docId, int typeId) {
    final var response = client
        .target(HOST + "/rest/files/")
        .request()
        .post(entity("{\"docId\":  \"" + docId + "\", \"typeId\":  \"" + typeId + "\"}", APPLICATION_JSON_TYPE));

    var body = response.readEntity(String.class);
    return JsonPath.parse(body).read("$.id");
  }

  public static String createVersion(String fileId, String content) {
    var multiPart = new FormDataMultiPart()
        .field("fileId", fileId)
        .field(
            "contents",
            content,
            APPLICATION_OCTET_STREAM_TYPE
        );

    var request = client
        .register(MultiPartFeature.class)
        .target(HOST + "/rest/versions")
        .request();

    var entity = entity(multiPart, multiPart.getMediaType());
    var response = request.post(entity);
    var body = response.readEntity(String.class);
    return JsonPath.parse(body).read("$.id");
  }
}
