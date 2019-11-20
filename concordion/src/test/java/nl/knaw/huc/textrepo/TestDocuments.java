package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.JsonPath;
import org.concordion.api.MultiValueResult;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.core.Response;

import static nl.knaw.huc.textrepo.Config.HTTP_APP_HOST;
import static nl.knaw.huc.textrepo.TestUtils.getLocation;
import static nl.knaw.huc.textrepo.TestUtils.getMultiPartEntity;

public class TestDocuments extends AbstractConcordionTest {
  private static final String HOST = HTTP_APP_HOST;
  private static final String DOCUMENTS_URL = HOST + "/documents";

  public MultiValueResult upload(String content) {
    var multiPart = new FormDataMultiPart().field("contents", content);

    var request = client()
        .register(MultiPartFeature.class)
        .target(DOCUMENTS_URL)
        .request();

    var response = request.post(getMultiPartEntity(multiPart));
    var locationHeader = getLocation(response);
    var optionalDocumentId = locationHeader.map(TestUtils::getDocumentId);
    var documentId = optionalDocumentId.orElse("No document id");

    return new MultiValueResult()
        .with("status", getStatus(response))
        .with("hasLocationHeader", locationHeader
            .map(l -> "has a Location header")
            .orElse("Missing Location header")
        )
        .with("location", locationHeader.orElse("No location"))
        .with("esLocation", "/documents/_doc/" + documentId)
        .with("documentId", documentId)
        .with("documentIdIsUUID", optionalDocumentId.map(TestUtils::isValidUUID).orElse("No document id"));
  }

  public MultiValueResult latest(Object loc) {
    var location = (String) loc;
    var request = client().target(location + "/files").request();
    var response = request.get();
    return new MultiValueResult()
        .with("status", getStatus(response))
        .with("entity", response.readEntity(String.class));
  }

  public MultiValueResult index(Object param) {
    var documentId = (String) param;
    var request = client().target(ES_HOST + "/documents/_doc/" + documentId).request();
    var response = request.get();
    return new MultiValueResult()
        .with("status", getStatus(response))
        .with("entity", JsonPath.parse(response.readEntity(String.class)).read("$._source.content"));
  }

  private static String getStatus(Response response) {
    return response.getStatus() + " " + response.getStatusInfo();
  }

}
