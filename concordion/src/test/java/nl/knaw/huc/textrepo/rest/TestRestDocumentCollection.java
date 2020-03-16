package nl.knaw.huc.textrepo.rest;

import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;

import javax.ws.rs.core.UriBuilder;

import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;

public class TestRestDocumentCollection extends AbstractConcordionTest {

  public void createDocument(String externalId) {
    RestUtils.createDocument(externalId);
  }

  public static class SearchResult {
    public int status;
    public String body;
    public int documentCount;
    public String externalId;
  }

  public SearchResult search(Object endpoint, String queryParam, String queryParamValue) {
    var url = HOST + endpoint.toString()
        + "?"
        + queryParam.replace("{externalId}", queryParamValue);
    System.out.println("document url:" + url);

    final var response = client
        .target(url)
        .request()
        .get();

    var result = new SearchResult();
    result.status = response.getStatus();
    var  body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    System.out.println("document body:" + body);
    var json = jsonPath.parse(body);
    result.documentCount = json.read("$.items.length()");
    result.externalId = json.read("$.items[0].externalId");
    return result;
  }

  public static class PaginateResult {
    public int status;
    public String body;
    public int itemCount;
    public int total;
    public String externalDocumentId;
  }

  public PaginateResult paginate(Object endpoint, String offset, String limit) {
    var url = UriBuilder
        .fromPath(HOST + endpoint.toString())
        .queryParam("offset", offset)
        .queryParam("limit", limit)
        .build();
    final var response = client
        .target(url)
        .request()
        .get();

    var result = new PaginateResult();
    result.status = response.getStatus();
    var  body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = jsonPath.parse(body);
    result.itemCount = json.read("$.items.length()");
    result.externalDocumentId = json.read("$.items[0].externalId");
    result.total = json.read("$.total", Integer.class);
    return result;
  }

}
