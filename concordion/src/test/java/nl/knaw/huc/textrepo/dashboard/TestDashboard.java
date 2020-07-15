package nl.knaw.huc.textrepo.dashboard;

import com.jayway.jsonpath.DocumentContext;
import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;

import javax.ws.rs.core.MediaType;

import static javax.ws.rs.client.Entity.entity;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;

public class TestDashboard extends AbstractConcordionTest {
  public String createDocument(String externalId) {
    return RestUtils.createDocument(externalId);
  }

  public String createFile(String docId) {
    return RestUtils.createFile(docId, getTextTypeId());
  }

  public void createMetadata(String docId, Object key, Object value) {
    client.target(HOST + "/rest/documents/" + docId + "/metadata/" + key)
          .request()
          .put(entity(value.toString(), MediaType.APPLICATION_JSON_TYPE));
  }

  public static class DashboardResult {
    public String status;
    public String body;
    public String documentCount;
    public String hasFile;
    public String hasMetadata;
    public String hasBoth;
    public String hasNone;
  }

  public DashboardResult retrieve(String endpoint) {
    final var response = client
        .target(HOST + endpoint)
        .request()
        .get();

    final var result = new DashboardResult();
    result.status = readableStatus(response);

    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);

    var json = jsonPath.parse(body);
    result.documentCount = json.read("$.documentCount") + " documents";
    result.hasFile = json.read("$.hasFile") + " documents with a file";
    result.hasMetadata = json.read("$.hasMetadata") + " documents with metadata";
    result.hasBoth = json.read("$.hasBoth") + " document with both a file and metadata";
    result.hasNone = json.read("$.hasNone") + " document without any files or metadata";

    return result;
  }

  public static class OrphansResult {
    public String status;
    public String body;
    public String itemCount;
    public String orphanExternalId;
    public String isPaginated;
  }

  public OrphansResult retrieveOrphans(String endpoint) {
    final var response = client
        .target(HOST + endpoint)
        .request()
        .get();

    final var result = new OrphansResult();
    result.status = readableStatus(response);

    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);

    var json = jsonPath.parse(body);
    int itemCount = json.read("$.total");
    result.itemCount = String.format("%d item%s", itemCount, itemCount == 1 ? "" : "s");
    result.orphanExternalId = "externalId: " + json.read("$.items[*].externalId");
    result.isPaginated = isPaginated(json) ? "is properly paginated" : "lacks proper pagination";
    return result;
  }

  private boolean isPaginated(DocumentContext json) {
    return json.read("$.items") != null
        && json.read("$.total") != null
        && json.read("$.page") != null
        && json.read("$.page.limit") != null
        && json.read("$.page.offset") != null;
  }

}
