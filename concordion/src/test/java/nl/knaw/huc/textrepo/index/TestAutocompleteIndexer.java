package nl.knaw.huc.textrepo.index;

import com.jayway.jsonpath.JsonPath;
import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static nl.knaw.huc.textrepo.Config.AUTOCOMPLETE_INDEX;
import static nl.knaw.huc.textrepo.util.IndexUtils.indexToUrl;
import static nl.knaw.huc.textrepo.util.IndexUtils.refreshIndex;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.isValidUuid;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class TestAutocompleteIndexer extends AbstractConcordionTest {

  private final String esQuery = "{\n" +
      "  \"suggest\": {\n" +
      "    \"keyword-suggest\": {\n" +
      "      \"prefix\":\"%prefix%\",\n" +
      "      \"completion\": {\n" +
      "        \"field\": \"suggest\",\n" +
      "        \"skip_duplicates\": true,\n" +
      "        \"size\": 5\n" +
      "      }\n" +
      "    }\n" +
      "  },\n" +
      "  \"_source\": \"suggest\"\n" +
      "}";

  public String createDocument() {
    return RestUtils.createDocument("test-" + randomAlphabetic(5));
  }

  public String createFile(String docId) {
    return RestUtils.createFile(docId, textTypeId);
  }

  public static class UploadResult {
    public String validUuid1;
    public String validUuid2;
    public String validUuid3;
    public String validVersions;
  }

  public UploadResult upload(
      String fileId1, String content1,
      String fileId2, String content2,
      String fileId3, String content3
  ) {
    var result = new UploadResult();
    result.validUuid1 = RestUtils.createVersion(fileId1, content1);
    result.validUuid2 = RestUtils.createVersion(fileId2, content2);
    result.validUuid3 = RestUtils.createVersion(fileId3, content3);
    result.validVersions =
        isValidUuid(result.validUuid1) && isValidUuid(result.validUuid2) && isValidUuid(result.validUuid3)
            ? "valid versions"
            : "one or more invalid UUIDs";
    return result;
  }

  public static class AutocompleteResult {
    public String suggestion1;
    public String suggestion2;
    public String suggestion3;
    public String body;
  }

  public AutocompleteResult searchAutocomplete(String prefix) {
    refreshIndex(client(), AUTOCOMPLETE_INDEX);

    var query = esQuery.replace("%prefix%", prefix);

    var response = client()
        .register(MultiPartFeature.class)
        .target(indexToUrl(AUTOCOMPLETE_INDEX) + "/_search")
        .request()
        .post(entity(query, APPLICATION_JSON_TYPE));

    var body = response.readEntity(String.class);
    System.out.println("es body: " + body);
    var parsedJson = JsonPath.parse(body);
    String suggestion1 = parsedJson.read("$.suggest['keyword-suggest'][0].options[0].text");
    String suggestion2 = parsedJson.read("$.suggest['keyword-suggest'][0].options[1].text");
    String suggestion3 = parsedJson.read("$.suggest['keyword-suggest'][0].options[2].text");

    var result = new AutocompleteResult();
    result.suggestion1 = suggestion1;
    result.suggestion2 = suggestion2;
    result.suggestion3 = suggestion3;
    result.body = asPrettyJson(body);
    return result;
  }

  public String getEsQuery() {
    return asPrettyJson(this.esQuery);
  }

}
