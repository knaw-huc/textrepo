package nl.knaw.huc.textrepo.rest;

import net.minidev.json.JSONArray;
import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;

import static java.lang.String.format;
import static java.util.Map.of;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceInUrlAndQueryParams;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceUrlParams;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class TestRestDocumentFiles extends AbstractConcordionTest {

  private String docId;
  private String textFileId;
  private String fooFileId;

  public void createDocumentWithTwoFiles() {
    this.docId = RestUtils.createDocument("dummy-" + randomAlphabetic(5));
    this.textFileId = RestUtils.createFile(docId, textTypeId);
    this.fooFileId = RestUtils.createFile(docId, fooTypeId);
  }

  public String getDocId() {
    return docId;
  }

  public String getTextFileId() {
    return textFileId;
  }

  public String getFooFileId() {
    return fooFileId;
  }

  public static class RetrieveResult {
    public int status;
    public String body;
    public int count;
    public String type1;
    public String type2;
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
    result.count = json.read("$.items.length()");

    var typeId1 = (int) json.read("$.items[?(@.id == \"" + textFileId + "\")].typeId", JSONArray.class).get(0);
    result.type1 = typeId1 == textTypeId ? "text" : format("[%d] != text type id [%d]", typeId1, textTypeId);

    var typeId2 = (int) json.read("$.items[?(@.id == \"" + fooFileId + "\")].typeId", JSONArray.class).get(0);
    result.type2 = typeId2 == fooTypeId ? "foo" : format("[%d] != foo type id [%d]", typeId2, fooTypeId);

    return result;
  }

  public static class PaginateResult {
    public int status;
    public String body;
    public String hasOld;
    public String externalDocumentId;
    public int total;
  }

  public PaginateResult paginate(Object endpoint, String docId, String offset, String limit, String textFileId) {
    var url = replaceInUrlAndQueryParams(endpoint, of(
        "{id}", docId,
        "{offset}", offset,
        "{limit}", limit
    ));

    var response = client
        .target(url)
        .request()
        .get();

    var result = new PaginateResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    System.out.println("document files body: " + body);
    result.body = asPrettyJson(body);
    var json = jsonPath.parse(body);
    var versionId = json.read("$.items[0].id", String.class);
    result.hasOld = versionId.equals(textFileId) ? "text" : format("[%s] isn't [%s]", versionId, textFileId);
    result.externalDocumentId = json.read("$.items[0].externalId");
    result.total = json.read("$.total", Integer.class);
    return result;
  }

  public PaginateResult filter(Object endpoint, String docId, int typeId, String textFileId) {
    var url = replaceInUrlAndQueryParams(endpoint, of(
        "{id}", docId,
        "{typeId}", "" + typeId
    ));

    var response = client
        .target(url)
        .request()
        .get();

    var result = new PaginateResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    System.out.println("document files body: " + body);
    result.body = asPrettyJson(body);
    var json = jsonPath.parse(body);
    var versionId = json.read("$.items[0].id", String.class);
    result.hasOld = versionId.equals(textFileId) ? "text" : format("[%s] isn't [%s]", versionId, textFileId);
    result.externalDocumentId = json.read("$.items[0].externalId");
    result.total = json.read("$.total", Integer.class);
    return result;
  }


}
