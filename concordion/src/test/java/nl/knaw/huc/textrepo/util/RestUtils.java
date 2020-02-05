package nl.knaw.huc.textrepo.util;

import com.jayway.jsonpath.JsonPath;

import javax.ws.rs.client.Client;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.Config.TYPES_URL;

public class RestUtils {

  public static String createDocument(Client client) {
    final var response = client
        .target(HOST + "/rest/documents")
        .request()
        .post(entity("{\"externalId\":  \"dummy\"}", APPLICATION_JSON_TYPE));

    var body = response.readEntity(String.class);
    return JsonPath.parse(body).read("$.id");
  }

  public static int createType(Client client, String type, String mimetype) {
    var response = client
        .target(TYPES_URL)
        .request()
        .post(json(format("{\"name\": \"%s\",\"mimetype\": \"%s\"}", type, mimetype)));
    var body = response.readEntity(String.class);
    return JsonPath.parse(body).read("$.id");
  }

}
