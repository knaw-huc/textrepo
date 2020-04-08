package nl.knaw.huc.textrepo.rest;

import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;

import static java.lang.String.format;
import static java.util.Map.of;
import static java.util.concurrent.TimeUnit.SECONDS;
import static nl.knaw.huc.textrepo.Config.HOST;
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

  public String createVersionWithDelay(String fileId) throws InterruptedException {
    SECONDS.sleep(2);
    return RestUtils.createVersion(fileId, "random-content-" + randomAlphabetic(10));
  }

  public String getLastVersionCreatedAt() {
    return null;
  }

  public String getCreatedAt(String newVersionId) {
    final var response = client
        .target(HOST + "/rest/versions/" + newVersionId)
        .request()
        .get();
    String s = response.readEntity(String.class);
    System.out.println("response getCreatedAt: " + s);
    return jsonPath
        .parse(s)
        .read("$.createdAt", String.class);
  }

  public static class RetrieveResult {
    public int status;
    public String body;
    public String twoVersions;
  }

  public RetrieveResult retrieve(Object endpoint, Object id, Object oldVersion, Object newVersion) {
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
    var hasOld = json.read("$.items[0].id", String.class).equals(oldVersion);
    var hasNew = json.read("$.items[1].id", String.class).equals(newVersion);
    result.twoVersions = hasOld && hasNew ? "old and new" : format("old [%s] and new [%s]", hasOld, hasNew);
    return result;
  }

  public static class PaginateResult {
    public int status;
    public String body;
    public String hasOld;
    public String externalDocumentId;
    public int total;
  }

  public PaginateResult paginate(Object endpoint, String fileId, String offset, String limit, String oldVersion) {
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
    var versionId = json.read("$.items[0].id", String.class);
    result.hasOld = versionId.equals(oldVersion) ? "old" : format("[%s] isn't [%s]", versionId, oldVersion);
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

  public CreatedAfterResult filterByCreatedAfter(String endpoint, String fileId, String date, String newVersion) {
    System.out.printf("endpoint + fileId + date: %s + %s + %s", endpoint, fileId, date);
    var url = createUrlQueryParams(endpoint, of(
        "{id}", fileId,
        "{date}", date
    ));

    var response = client
        .target(url)
        .request()
        .get();

    var result = new CreatedAfterResult();
    result.status = response.getStatus();
    var  body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = jsonPath.parse(body);
    var versionId = json.read("$.items[0].id", String.class);
    result.hasNew = versionId.equals(newVersion) ? "new" : format("[%s] isn't [%s]", versionId, newVersion);
    result.total = json.read("$.total", Integer.class);
    return result;
  }


}
