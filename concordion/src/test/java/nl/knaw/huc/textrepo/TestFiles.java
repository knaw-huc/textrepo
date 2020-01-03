package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.JsonPath;
import nl.knaw.huc.textrepo.util.TestUtils;
import org.concordion.api.MultiValueResult;

import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static nl.knaw.huc.textrepo.Config.FILES_URL;
import static nl.knaw.huc.textrepo.util.TestUtils.getLocation;
import static nl.knaw.huc.textrepo.util.TestUtils.postFileWithFilename;

public class TestFiles extends AbstractConcordionTest {

  public MultiValueResult upload(String content) throws MalformedURLException {
    var filename = "test-" + UUID.randomUUID() + ".txt";
    var endpoint = new URL(FILES_URL);
    var response = postFileWithFilename(client(), endpoint, filename, content.getBytes());

    var locationHeader = getLocation(response);
    var optionalFileId = locationHeader.map(TestUtils::getFileId);
    var fileId = optionalFileId.orElse("No file id");

    return new MultiValueResult()
        .with("status", getStatus(response))
        .with("hasLocationHeader", locationHeader
            .map(l -> "has a Location header")
            .orElse("Missing Location header")
        )
        .with("location", locationHeader.orElse("No location"))
        .with("esLocation", "/files/_doc/" + fileId)
        .with("fileId", fileId)
        .with("fileIdIsUUID", optionalFileId.map(TestUtils::isValidUuidMsg).orElse("No file id"));
  }

  public MultiValueResult latest(Object loc) {
    var location = (String) loc;
    var request = client().target(location + "/contents").request();
    var response = request.get();
    return new MultiValueResult()
        .with("status", getStatus(response))
        .with("entity", response.readEntity(String.class));
  }

  public MultiValueResult index(Object param) {
    var fileId = (String) param;
    var request = client().target(ES_HOST + "/files/_doc/" + fileId).request();
    var response = request.get();
    return new MultiValueResult()
        .with("status", getStatus(response))
        .with("entity", JsonPath.parse(response.readEntity(String.class)).read("$._source.content"));
  }

  private static String getStatus(Response response) {
    return response.getStatus() + " " + response.getStatusInfo();
  }

}
