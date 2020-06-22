package nl.knaw.huc.textrepo.rest;

import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;

import static java.lang.String.format;
import static java.util.Map.of;
import static java.util.concurrent.TimeUnit.SECONDS;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceInUrlAndQueryParams;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class TestRestDocumentCollection extends AbstractConcordionTest {

  public void createDocument(String externalId) {
    RestUtils.createDocument(externalId);
  }

  public String createDocumentWithDelay() throws InterruptedException {
    SECONDS.sleep(2);
    return RestUtils.createDocument("random-external-id-" + randomAlphabetic(10));
  }

  public String getCreatedAt(String newDocumentId) {
    var response = client
        .target(HOST + "/rest/documents/" + newDocumentId)
        .request()
        .get();
    var s = response.readEntity(String.class);
    return jsonPath
        .parse(s)
        .read("$.createdAt", String.class);
  }

  public static class SearchResult {
    public int status;
    public String body;
    public int documentCount;
    public String externalId;
  }

  public SearchResult search(Object endpoint, String externalId) {
    var url = replaceInUrlAndQueryParams(endpoint, of("{externalId}", externalId));

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
    var url = replaceInUrlAndQueryParams(endpoint, of("{offset}", offset, "{limit}", limit));
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

  public static class CreatedAfterResult {
    public int status;
    public String body;
    public String hasNew;
    public int total;
  }

  public CreatedAfterResult filterByCreatedAfter(String endpoint, String date, String newDocument) {
    System.out.printf("endpoint + fileId + date: %s + %s", endpoint, date);
    var url = replaceInUrlAndQueryParams(endpoint, of(
        "{date}", date
    ));

    var response = client
        .target(url)
        .request()
        .get();

    var result = new CreatedAfterResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = jsonPath.parse(body);
    var versionId = json.read("$.items[0].id", String.class);
    result.hasNew = versionId.equals(newDocument) ? "new" : format("[%s] isn't [%s]", versionId, newDocument);
    result.total = json.read("$.total", Integer.class);
    return result;
  }

}
