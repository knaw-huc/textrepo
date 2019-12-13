package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.JsonPath;
import org.concordion.api.MultiValueResult;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Client;

import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static nl.knaw.huc.textrepo.Config.AUTOCOMPLETE_INDEX;
import static nl.knaw.huc.textrepo.Config.FILES_URL;
import static nl.knaw.huc.textrepo.TestUtils.getLocation;
import static nl.knaw.huc.textrepo.TestUtils.getMultiPartEntity;
import static nl.knaw.huc.textrepo.TestUtils.isValidUuid;

public class TestCustomIndexer extends AbstractConcordionTest {

  public static class UploadResult {
    public String validUuid1;
    public String validUuid2;
    public String validUuid3;
    public String validUuids;
  }

  public UploadResult upload(String content1, String content2, String content3) {
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
    // wait for docs to be indexed:
    try {
      SECONDS.sleep(1);
    } catch (InterruptedException e) {
      logger.error("Could not wait");
    }

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
        .target(ES_HOST + "/" + AUTOCOMPLETE_INDEX + "/_search")
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

  public static String uploadFile(String content, Client client) {
    var multiPart = new FormDataMultiPart().field("contents", content);

    var request = client
        .register(MultiPartFeature.class)
        .target(FILES_URL)
        .request();

    var response = request.post(getMultiPartEntity(multiPart));
    var locationHeader = getLocation(response);
    var fileId = locationHeader
        .map(TestUtils::getFileId)
        .orElseThrow();
    return fileId;
  }

}
