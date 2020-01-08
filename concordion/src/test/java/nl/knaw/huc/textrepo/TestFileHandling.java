package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.JsonPath;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Entity;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static nl.knaw.huc.textrepo.Config.FILES_URL;
import static nl.knaw.huc.textrepo.util.TestUtils.postFileWithFilename;

public class TestFileHandling extends AbstractConcordionTest {

  private static Entity<FormDataMultiPart> multiPartEntity(FormDataMultiPart multiPart) {
    return Entity.entity(multiPart, multiPart.getMediaType());
  }

  public UploadResult upload(String content) throws MalformedURLException {
    var filename = "test-" + UUID.randomUUID() + ".txt";
    var endpoint = new URL(FILES_URL);
    var response = postFileWithFilename(client(), endpoint, filename, content.getBytes());

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
