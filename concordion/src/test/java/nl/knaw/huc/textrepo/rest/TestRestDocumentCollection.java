package nl.knaw.huc.textrepo.rest;

import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;
import nl.knaw.huc.textrepo.util.TestUtils;

import javax.ws.rs.core.UriBuilder;

import static java.util.Map.of;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.createUrlQueryParams;

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

  public SearchResult search(Object endpoint, String externalId) {
    var url = createUrlQueryParams(endpoint, of("{externalId}", externalId));

    final var response = client
        .target(url)
        .request()
        .get();

    var result = new SearchResult();
    result.status = response.getStatus();
    var  body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
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
    var url = createUrlQueryParams(endpoint, of("{offset}", offset, "{limit}", limit));
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
