package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.JsonPath;

import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;

import static nl.knaw.huc.textrepo.Config.HTTP_APP_HOST;
import static nl.knaw.huc.textrepo.TestUtils.getDocumentId;
import static nl.knaw.huc.textrepo.TestUtils.getLocation;
import static nl.knaw.huc.textrepo.TestUtils.putFileWithFilename;
import static nl.knaw.huc.textrepo.TestUtils.replace;

public class TestVersions extends AbstractConcordionTest {

  public static class TestVersionsResult {
    public int status;
    public int statusUpdate;
    public String documentId;
    public String version1Sha;
    public String version2Sha;
    public String indexContentAfterUpdate;
  }

  public TestVersionsResult uploadMultipleVersions(
      String content,
      String documentsEndpoint,
      String newContent,
      String documentFilesEndpoint
  ) throws MalformedURLException {
    var result = new TestVersionsResult();

    var response = postFile(documentsEndpoint, content);

    result.status = response.getStatus();
    result.documentId = getDocumentId(getLocation(response).orElse("/no-document-uuid"));

    result.statusUpdate = updateDocument(documentFilesEndpoint, newContent, result);

    var jsonVersions = getVersions(result.documentId);

    result.version1Sha = jsonPath.parse(jsonVersions).read("$[0].contentsSha");
    result.version2Sha = jsonPath.parse(jsonVersions).read("$[1].contentsSha");

    result.indexContentAfterUpdate = getIndexDocument(result.documentId);
    return result;
  }

  private int updateDocument(String documentFilesEndpoint, String newContent, TestVersionsResult result) {
    return putFileWithFilename(
        client(),
        replace(HTTP_APP_HOST + documentFilesEndpoint, "documentId", result.documentId),
        "test.txt",
        newContent.getBytes()
    ).getStatus();
  }

  private Response postFile(String documentsEndpoint, String content) throws MalformedURLException {
    return TestUtils.postFileWithFilename(client(), new URL(HTTP_APP_HOST + documentsEndpoint), "test.txt", content.getBytes());
  }

  private String getVersions(String documentId) {
    var url = String.format("%s/documents/%s/versions", APP_HOST, documentId);
    return getByUrl(url);
  }

  private String getIndexDocument(String documentId) {
    var url = ES_HOST + "/documents/_doc/" + documentId;
    return JsonPath.parse(getByUrl(url)).read("$._source.content");
  }

  private String getByUrl(String url) {
    return client()
        .target(url)
        .request()
        .get()
        .readEntity(String.class);
  }

}
