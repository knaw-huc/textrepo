package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.JsonPath;
import org.concordion.integration.junit4.ConcordionRunner;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

@RunWith(ConcordionRunner.class)
public class TestFileHandling {
  public static String responsePart(Response response, String s) {
    return JsonPath.parse(response.readEntity(String.class)).read(s);
  }

  public Result upload(String content) {
    var multiPart = new FormDataMultiPart().field("file", content);

    var client = JerseyClientBuilder.newClient();
    var entity = Entity.entity(multiPart, multiPart.getMediaType());
    var request = client
        .register(MultiPartFeature.class)
        .target("http://localhost:8080/textrepo/files")
        .request();

    var response = request.post(entity);
    final var sha224 = responsePart(response, "$.sha224");

    final Result result = new Result();
    result.status = response.getStatus();
    result.sha224 = sha224;
    result.location = response.getHeaderString("Location");
    return result;
  }

  class Result {
    public int status;
    public String sha224;
    public String location;
  }
}
