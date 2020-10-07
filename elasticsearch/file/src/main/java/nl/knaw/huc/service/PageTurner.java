package nl.knaw.huc.service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.TypeRef;
import nl.knaw.huc.api.FormVersion;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static nl.knaw.huc.service.ResourceUtil.read;

/**
 * Turn the pages of a paginated resource based on:
 * - query params limit
 * - zero-based offset
 * For every page: pass json contents of `$.items` to reader
 */
public class PageTurner<T> {

  private static final Client client = JerseyClientBuilder.newClient();
  private final ParseContext jsonPath;
  private final URI paginated;
  private final int limit;
  private int offset;

  public PageTurner(String url, int offset, int limit, ParseContext jsonPath) {
    try {
      this.paginated = new URL(url).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new IllegalArgumentException("Could not create url of: " + url);
    }
    this.offset = offset;
    this.limit = limit;
    this.jsonPath = jsonPath;
  }

  /**
   * Turn the pages and pass the `$.items` of each page to reader
   */
  public void turn(Consumer<List<FormVersion>> reader) {

    // set total number of items after retrieving first page:
    var total = -1;

    while (total == -1 || total > offset) {
      var page = getPage(limit, offset);
      if (total == -1) {
        total = read(page, "$.total");
      }
      TypeRef<ArrayList<FormVersion>> typeRef = new TypeRef<>() {};
      ArrayList<FormVersion> read = read(page, "items", typeRef);
      reader.accept(read);
      offset = offset + limit;
    }
  }

  private DocumentContext getPage(int limit, int offset) {
    var response = client
        .target(paginated)
        .queryParam("limit", limit)
        .queryParam("offset", offset)
        .request()
        .get();

    var body = response.readEntity(String.class);
    return jsonPath.parse(body);
  }

}
