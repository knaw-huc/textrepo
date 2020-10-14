package nl.knaw.huc.service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.TypeRef;
import nl.knaw.huc.exception.TextRepoRequestException;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static java.lang.String.format;

public class ResourceUtil {

  /**
   * Read jsonpath in parsed doc
   * @throws TextRepoRequestException with msg containing path and json body
   */
  public static <T> T read(DocumentContext doc, String path) {
    TypeRef<T> typeRef = new TypeRef<>() {};
    return read(doc, path, typeRef);
  }

  /**
   * Read jsonpath in parsed doc with explicit type
   * @throws TextRepoRequestException with msg containing path and json body
   */
  public static <T> T read(DocumentContext doc, String path, TypeRef<T> typeRef) {
    try {
      return doc.read(path, typeRef);
    } catch (Exception ex) {
      throw new TextRepoRequestException(format(
          "Could not get path %s in json %s",
          path, doc.jsonString()
      ), ex);
    }
  }

  public static Response getResource(String url, Invocation.Builder request) {
    var response = request.get();
    if (response.getStatus() != 200) {
      throw new TextRepoRequestException(format(
          "Unexpected response status of [%s]: got %s instead of 200",
          url, response.getStatus()
      ));
    }
    return response;
  }

}
