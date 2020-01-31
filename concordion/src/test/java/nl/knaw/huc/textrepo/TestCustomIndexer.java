package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.JsonPath;
import nl.knaw.huc.textrepo.util.TestUtils;
import org.concordion.api.MultiValueResult;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Client;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static nl.knaw.huc.textrepo.Config.AUTOCOMPLETE_INDEX;
import static nl.knaw.huc.textrepo.Config.FILES_URL;
import static nl.knaw.huc.textrepo.util.IndexUtils.indexToUrl;
import static nl.knaw.huc.textrepo.util.IndexUtils.refreshIndex;
import static nl.knaw.huc.textrepo.util.TestUtils.getLocation;
import static nl.knaw.huc.textrepo.util.TestUtils.isValidUuid;
import static nl.knaw.huc.textrepo.util.TestUtils.postFileWithFilename;

import org.junit.Ignore; @Ignore public class TestCustomIndexer extends AbstractConcordionTest {

  public static class UploadResult {
    public String validUuid1;
    public String validUuid2;
    public String validUuid3;
    public String validUuids;
  }

  public UploadResult upload(String content1, String content2, String content3) throws MalformedURLException {
    var result = new UploadResult();
    result.validUuid1 = uploadFile(content1, client());
    result.validUuid2 = uploadFile(content2, client());
    result.validUuid3 = uploadFile(content3, client());
    result.validUuids =
        isValidUuid(result.validUuid1) && isValidUuid(result.validUuid2) && isValidUuid(result.validUuid3)
            ? "valid UUIDs"
            : "one or more invalid UUIDs";
    return result;
  }

  public MultiValueResult searchAutocomplete(String pre) {
    refreshIndex(client(), AUTOCOMPLETE_INDEX);

    var query = "{" +
        "  \"suggest\": {" +
        "    \"keyword-suggest\": {" +
        "      \"prefix\": \"" + pre + "\"," +
        "      \"completion\": {" +
        "        \"field\": \"suggest\"," +
        "        \"skip_duplicates\": true," +
        "        \"size\": 5" +
        "      }" +
        "    }" +
        "  }," +
        "  \"_source\": \"suggest\"" +
        "}";

    var request = client()
        .register(MultiPartFeature.class)
        .target(indexToUrl(AUTOCOMPLETE_INDEX) + "/_search")
        .request()
        .post(entity(query, APPLICATION_JSON_TYPE));

    var json = request.readEntity(String.class);
    var parsedJson = JsonPath.parse(json);
    String suggestion1 = parsedJson.read("$.suggest['keyword-suggest'][0].options[0].text");
    String suggestion2 = parsedJson.read("$.suggest['keyword-suggest'][0].options[1].text");
    String suggestion3 = parsedJson.read("$.suggest['keyword-suggest'][0].options[2].text");

    return new MultiValueResult()
        .with("json", json)
        .with("suggestion1", suggestion1)
        .with("suggestion2", suggestion2)
        .with("suggestion3", suggestion3);
  }

  private static String uploadFile(String content, Client client) throws MalformedURLException {
    var filename = "test-" + UUID.randomUUID() + ".txt";
    var endpoint = new URL(FILES_URL);
    var response = postFileWithFilename(client, endpoint, filename, content.getBytes());

    var locationHeader = getLocation(response);
    var fileId = locationHeader
        .map(TestUtils::getFileId)
        .orElseThrow();
    return fileId;
  }

}
