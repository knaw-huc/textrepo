package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.JsonPath;
import org.concordion.api.MultiValueResult;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.core.Response;

import static nl.knaw.huc.textrepo.Config.FILES_URL;
import static nl.knaw.huc.textrepo.TestUtils.getLocation;
import static nl.knaw.huc.textrepo.TestUtils.getMultiPartEntity;

public class TestFiles extends AbstractConcordionTest {

  public MultiValueResult upload(String content) {
    var multiPart = new FormDataMultiPart().field("contents", content);

    var request = client()
        .register(MultiPartFeature.class)
        .target(FILES_URL)
        .request();

    var response = request.post(getMultiPartEntity(multiPart));
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
