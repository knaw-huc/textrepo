package nl.knaw.huc.textrepo.rest;

import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import static nl.knaw.huc.textrepo.util.TestUtils.asCodeBlock;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceUrlParams;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class TestRestContents extends AbstractConcordionTest {

  public String createDocument() {
    return RestUtils.createDocument("dummy-" + randomAlphabetic(5));
  }

  public String createFile(String docId) {
    return RestUtils.createFile(docId, textTypeId);
  }

  public String createVersion(String fileId) {
    return RestUtils.createVersion(fileId, "some scrumptious content");
  }

  public String getSha(String versionId) {
    var request = client
        .register(MultiPartFeature.class)
        .target(replaceUrlParams("/rest/versions/{id}", versionId))
        .request();

    var response = request.get();
    var body = response.readEntity(String.class);
    return jsonPath.parse(body).read("$.contentsSha");
  }

  public static class RetrieveResult {
    public int status;
    public String contents;
    public String body;
  }

  public RetrieveResult retrieve(Object endpoint, Object id) {
    final var response = client
        .target(replaceUrlParams(endpoint, id))
        .request()
        .get();

    var result = new RetrieveResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.contents = body;
    result.body = asCodeBlock(body);
    return result;
  }

}
