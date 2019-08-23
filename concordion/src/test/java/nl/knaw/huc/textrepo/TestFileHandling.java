package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.JsonPath;
import org.concordion.integration.junit4.ConcordionRunner;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;

@RunWith(ConcordionRunner.class)
public class TestFileHandling {
  private static Client client() {
    return JerseyClientBuilder.newClient();
  }

  private static Entity<FormDataMultiPart> multiPartEntity(FormDataMultiPart multiPart) {
    return Entity.entity(multiPart, multiPart.getMediaType());
  }

  public UploadResult upload(String content) {
    var multiPart = new FormDataMultiPart().field("file", content);

    var request = client()
        .register(MultiPartFeature.class)
        .target("http://localhost:8080/textrepo/files")
        .request();

    var response = request.post(multiPartEntity(multiPart));
    var entity = response.readEntity(String.class);

    var result = new UploadResult();
    result.status = response.getStatus();
    result.sha224 = JsonPath.parse(entity).read("$.sha224");
    result.location = response.getHeaderString("Location");
    return result;
  }

  class UploadResult {
    public int status;
    public String sha224;
    public String location;

  }

  public RetrievalResult retrieve(String uri) {
    var url = "http://localhost:8080/textrepo/" + uri;
    var response = client().target(url).request().get();
    var entity = response.readEntity(String.class);

    var result = new RetrievalResult();
    result.status = response.getStatus();
    if (entity.contains("message")) {
      result.content = "";
      result.message = JsonPath.parse(entity).read("$.message");
    } else {
      result.content = entity;
      result.message = "";
    }

    return result;
  }

  class RetrievalResult {
    public int status;
    public String content;
    public String message;
  }
}
