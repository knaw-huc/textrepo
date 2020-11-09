package nl.knaw.huc.textrepo.util;

import com.jayway.jsonpath.JsonPath;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Client;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.Config.TYPES_URL;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceUrlParams;

public class RestUtils {

  private static Client client = JerseyClientBuilder.newClient();

  /**
   * @return String document uuid
   */
  public static String createDocument(String externalId) {
    final var response = client
        .target(HOST + "/rest/documents")
        .request()
        .post(entity("{\"externalId\":  \"" + externalId + "\"}", APPLICATION_JSON_TYPE));

    var body = response.readEntity(String.class);
    return JsonPath.parse(body).read("$.id");
  }

  public static void createDocumentMetadata(String id, String key, String value) {
    createMetadata(id, key, value, "/rest/documents/{id}/metadata/{key}");
  }

  public static void createFileMetadata(String id, String key, String value) {
    createMetadata(id, key, value, "/rest/files/{id}/metadata/{key}");
  }

  private static void createMetadata(String id, String key, String value, String endpoint) {
    var url = replaceUrlParams(endpoint, id, key);
    client
        .target(url)
        .request()
        .put(entity(value, TEXT_PLAIN));
  }

  public static int createType(String type, String mimetype) {
    var response = client
        .target(TYPES_URL)
        .request()
        .post(json(format("{\"name\": \"%s\",\"mimetype\": \"%s\"}", type, mimetype)));
    var body = response.readEntity(String.class);
    return JsonPath.parse(body).read("$.id");
  }

  /**
   * @return String file id
   */
  public static String createFile(String docId, int typeId) {
    final var response = client
        .target(HOST + "/rest/files/")
        .request()
        .post(entity("{\"docId\":  \"" + docId + "\", \"typeId\":  \"" + typeId + "\"}", APPLICATION_JSON_TYPE));

    var body = response.readEntity(String.class);
    return JsonPath.parse(body).read("$.id");
  }

  /**
   * @return String file version id
   */
  public static String createVersion(String fileId, String contents) {
    var multiPart = new FormDataMultiPart()
        .field("fileId", fileId)
        .field(
            "contents",
            contents,
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
