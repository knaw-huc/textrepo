package nl.knaw.huc.textrepo.dashboard;

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
    public int status;
    public String body;
  }

  public DashboardResult retrieve(String endpoint) {
    final var response = client
        .target(HOST + endpoint)
        .request()
        .get();

    final var result = new DashboardResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    return result;
  }
}
