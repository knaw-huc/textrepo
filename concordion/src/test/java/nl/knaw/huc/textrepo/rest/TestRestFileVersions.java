package nl.knaw.huc.textrepo.rest;

import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;

import static java.util.Map.of;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceUrlParams;
import static nl.knaw.huc.textrepo.util.TestUtils.createUrlQueryParams;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class TestRestFileVersions extends AbstractConcordionTest {

  public String createDocument() {
    return RestUtils.createDocument("dummy-" + randomAlphabetic(5));
  }

  public String createFile(String docId) {
    return RestUtils.createFile(docId, textTypeId);
  }

  public String createVersion(String fileId) {
    return RestUtils.createVersion(fileId, "random-content-" + randomAlphabetic(10));
  }

  public static class RetrieveResult {
    public int status;
    public String body;
    public String twoVersions;
  }

  public RetrieveResult retrieve(Object endpoint, Object id) {
    final var response = client
        .target(replaceUrlParams(endpoint, id))
        .request()
        .get();

    var result = new RetrieveResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = jsonPath.parse(body);
    var length = json.read("$.items.length()", Integer.class);
    result.twoVersions = "" + length;
    return result;
  }

  public static class PaginateResult {
    public int status;
    public String body;
    public int itemCount;
    public String externalDocumentId;
    public int total;
  }

  public PaginateResult paginate(Object endpoint, String fileId, String offset, String limit) {
    var url = createUrlQueryParams(endpoint, of(
        "{id}", fileId,
        "{offset}", offset,
        "{limit}", limit
    ));

    var response = client
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
