package nl.knaw.huc.textrepo.task;

import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;
import org.apache.commons.text.StringEscapeUtils;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.util.TestUtils.asCodeBlock;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceUrlParams;

public class TestFindDocumentMetadataByExternalId extends AbstractConcordionTest {

  public String createDocument(String externalId) {
    return RestUtils.createDocument(externalId);
  }

  public void createMetadata(Object docId, Object key, Object value) {
    client
        .target(HOST + "/rest/documents/" + docId + "/metadata/" + key)
        .request()
        .put(entity(value.toString(), APPLICATION_JSON_TYPE));
  }

  public static class RetrieveResult {
    public int status;
    public String value;
    public String original;
    public String parent;
    public String headers;
    public String body;
  }

  public RetrieveResult retrieve(Object endpoint, Object docId, Object externalId, Object key) {
    final var response = client
        .target(replaceUrlParams(endpoint, externalId))
        .request()
        .get();

    var result = new RetrieveResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = jsonPath.parse(body);
    result.value = json.read("$." + key);

    result.headers = "";
    var links = response.getHeaders().get("Link");
    links.forEach((l) -> result.headers += asHeaderLink(l.toString()));

    result.original = links
        .stream()
        .filter(l -> l.toString().contains("/metadata"))
        .findFirst()
        .map(l -> "original resource")
        .orElse("header link missing");

    result.parent = links
        .stream()
        .filter((l) -> !l.toString().contains("/metadata"))
        .findFirst()
        .map(l -> "parent resource")
        .orElse("header link missing");

    result.headers = asCodeBlock(result.headers);

    return result;
  }

  private String asHeaderLink(String header) {
    return StringEscapeUtils.escapeHtml4("Link: " + header + "\n");
  }
}
