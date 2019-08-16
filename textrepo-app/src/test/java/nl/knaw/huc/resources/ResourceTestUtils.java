package nl.knaw.huc.resources;

import com.jayway.jsonpath.JsonPath;

import javax.ws.rs.core.Response;

public class ResourceTestUtils {

  public static String responsePart(Response response, String s) {
    return JsonPath.parse(response.readEntity(String.class)).read(s);
  }
}
