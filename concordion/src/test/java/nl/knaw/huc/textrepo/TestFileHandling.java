package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.JsonPath;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Entity;

public class TestFileHandling extends AbstractConcordionTest {

  private static Entity<FormDataMultiPart> multiPartEntity(FormDataMultiPart multiPart) {
    return Entity.entity(multiPart, multiPart.getMediaType());
  }

  public UploadResult upload(String content) {
    var multiPart = new FormDataMultiPart().field("contents", content);

    var request = client()
        .register(MultiPartFeature.class)
        .target(APP_HOST + "/files")
        .request();

    var response = request.post(multiPartEntity(multiPart));

    var result = new UploadResult();
    result.status = response.getStatus();
    var latestVersionLocation = response.getHeaderString("Location") + "/latest";
    logger.info("fileLocation: " + latestVersionLocation);

    var requestVersion = client()
        .register(MultiPartFeature.class)
        .target(latestVersionLocation)
        .request()
        .get();
    var versionJson = requestVersion.readEntity(String.class);
    logger.info("versionJson: " + versionJson);
    result.sha224 = JsonPath.parse(versionJson).read("$.contentsSha");

    return result;
  }

  class UploadResult {
    public int status;
    public String sha224;
    public String location;
  }

  public RetrievalResult retrieve(String uri) {
    var url = APP_HOST + uri;
    var response = client()
        .target(url)
        .request()
        .get();
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
