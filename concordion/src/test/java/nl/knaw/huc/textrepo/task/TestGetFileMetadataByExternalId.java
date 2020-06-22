package nl.knaw.huc.textrepo.task;

import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;
import org.apache.commons.text.StringEscapeUtils;

import static java.util.Map.of;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.util.TestUtils.asCodeBlock;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceInUrlAndQueryParams;

public class TestGetFileMetadataByExternalId extends AbstractConcordionTest {

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
        .put(entity(value.toString(), APPLICATION_JSON_TYPE));
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
        .target(replaceInUrlAndQueryParams(endpoint, of("{externalId}", externalId, "{typeName}", fileType)))
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

    var original = links
        .stream()
        .filter(l -> l.toString().contains("/metadata"))
        .findFirst();
    if (original.isPresent()) {
      result.original = "original resource";
      result.headers += asHeaderLink(original.get().toString());
    } else {
      result.original = "resource missing";
    }

    var parent = links
        .stream()
        .filter((l) -> l.toString().contains("/files") && !l.toString().contains("/metadata"))
        .findFirst();
    if (parent.isPresent()) {
      result.parent = "parent resource";
      result.headers += asHeaderLink(parent.get().toString());
    } else {
      result.parent = "resource missing";
    }

    var type = links
        .stream()
        .filter((l) -> l.toString().contains("/types/"))
        .findFirst();
    if (type.isPresent()) {
      result.type = "type resource";
      result.headers += asHeaderLink(type.get().toString());
    } else {
      result.type = "resource missing";
    }

    result.headers = asCodeBlock(result.headers);

    return result;
  }

  private String asHeaderLink(String header) {
    return StringEscapeUtils.escapeHtml4("Link: " + header + "\n");
  }
}
