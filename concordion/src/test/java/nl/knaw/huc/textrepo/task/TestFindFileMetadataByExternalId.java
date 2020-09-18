package nl.knaw.huc.textrepo.task;

import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;
import org.apache.commons.text.StringEscapeUtils;

import static java.util.Map.of;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.util.TestUtils.asCodeBlock;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceInUrlAndQueryParams;

public class TestFindFileMetadataByExternalId extends AbstractConcordionTest {

  public String createDocument(String externalId) {
    return RestUtils.createDocument(externalId);
  }

  public String createFile(String docId) {
    return RestUtils.createFile(docId, getTextTypeId());
  }

  public void createMetadata(Object fileId, Object key, Object value) {
    client
        .target(HOST + "/rest/files/" + fileId + "/metadata/" + key)
        .request()
        .put(entity(value.toString(), TEXT_PLAIN));
  }

  public static class RetrieveResult {
    public int status;
    public String value;
    public String original;
    public String parent;
    public String type;
    public String headers;
    public String body;
  }

  public RetrieveResult retrieve(String endpoint, String externalId, String fileType, String key) {
    final var response = client
        .target(replaceInUrlAndQueryParams(endpoint, of("{externalId}", externalId, "{name}", fileType)))
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
        .filter((l) -> l.toString().contains("/files") && !l.toString().contains("/metadata"))
        .findFirst()
        .map(l -> "parent resource")
        .orElse("header link missing");

    result.type = links
        .stream()
        .filter((l) -> l.toString().contains("/types/"))
        .findFirst()
        .map(l -> "type resource")
        .orElse("header link missing");

    result.headers = asCodeBlock(result.headers);

    return result;
  }

  private String asHeaderLink(String header) {
    return StringEscapeUtils.escapeHtml4("Link: " + header + "\n");
  }
}
