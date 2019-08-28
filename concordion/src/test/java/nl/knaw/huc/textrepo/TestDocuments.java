package nl.knaw.huc.textrepo;

import org.concordion.api.FullOGNL;
import org.concordion.api.MultiValueResult;
import org.concordion.integration.junit4.ConcordionRunner;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.UUID;

import static nl.knaw.huc.textrepo.Config.HTTP_APP_HOST;
import static nl.knaw.huc.textrepo.Config.HTTP_ES_HOST;

@FullOGNL
@RunWith(ConcordionRunner.class)
public class TestDocuments {
  private static final String HOST = HTTP_APP_HOST;
  private static final String DOCUMENTS_URL = HOST + "/documents";

  private static Client client() {
    return JerseyClientBuilder.newClient();
  }

  private static Entity<FormDataMultiPart> multiPartEntity(FormDataMultiPart multiPart) {
    return Entity.entity(multiPart, multiPart.getMediaType());
  }

  public MultiValueResult upload(String content) {
    var multiPart = new FormDataMultiPart().field("file", content);

    var request = client()
        .register(MultiPartFeature.class)
        .target(DOCUMENTS_URL)
        .request();

    var response = request.post(multiPartEntity(multiPart));
    var locationHeader = getLocation(response);
    var documentId = locationHeader.map(this::getDocumentId);

    return new MultiValueResult()
        .with("status", getStatus(response))
        .with("hasLocationHeader", locationHeader.map(l -> "has a Location header")
                                                 .orElse("Missing Location header"))
        .with("location", locationHeader.orElse("No location"))
        .with("documentId", documentId.orElse("No document id"))
        .with("documentIdIsUUID", documentId.map(this::isValidUUID).orElse("No document id"));
  }

  public MultiValueResult latest(Object loc) {
    String location = (String) loc;
    System.err.println("latest: " + location);
    var request = client().target(location + "/files").request();
    var response = request.get();
    return new MultiValueResult()
        .with("status", getStatus(response))
        .with("entity", response.readEntity(String.class));
  }

  private static String getStatus(Response response) {
    return response.getStatus() + " " + response.getStatusInfo();
  }

  private String isValidUUID(String documentId) {
    try {
      UUID.fromString(documentId);
      return "valid UUID";
    } catch (Exception e) {
      return "invalid UUID: " + e.getMessage();
    }
  }

  private String getDocumentId(String location) {
    return location.substring(location.lastIndexOf('/') + 1);
  }

  private Optional<String> getLocation(Response response) {
    return Optional.ofNullable(response.getHeaderString("Location"));
  }
}
