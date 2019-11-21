package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.JsonPath;

import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;

import static nl.knaw.huc.textrepo.Config.HTTP_APP_HOST;
import static nl.knaw.huc.textrepo.TestUtils.getFileId;
import static nl.knaw.huc.textrepo.TestUtils.getLocation;
import static nl.knaw.huc.textrepo.TestUtils.putFileWithFilename;
import static nl.knaw.huc.textrepo.TestUtils.replace;

public class TestVersions extends AbstractConcordionTest {

  public static class TestVersionsResult {
    public int status;
    public int statusUpdate;
    public String fileId;
    public String version1Sha;
    public String version2Sha;
    public String indexContentAfterUpdate;
  }

  public TestVersionsResult uploadMultipleVersions(
      String content,
      String filesEndpoint,
      String newContent,
      String fileContentsEndpoint
  ) throws MalformedURLException {
    var result = new TestVersionsResult();

    var response = postFile(filesEndpoint, content);

    result.status = response.getStatus();
    result.fileId = getFileId(getLocation(response).orElse("/no-file-uuid"));

    result.statusUpdate = updateFile(fileContentsEndpoint, newContent, result);

    var jsonVersions = getVersions(result.fileId);

    result.version1Sha = jsonPath.parse(jsonVersions).read("$[0].contentsSha");
    result.version2Sha = jsonPath.parse(jsonVersions).read("$[1].contentsSha");

    result.indexContentAfterUpdate = getIndexedFile(result.fileId);
    return result;
  }

  private int updateFile(String fileContentsEndpoint, String newContent, TestVersionsResult result) {
    return putFileWithFilename(
        client(),
        replace(HTTP_APP_HOST + fileContentsEndpoint, "fileId", result.fileId),
        "test.txt",
        newContent.getBytes()
    ).getStatus();
  }

  private Response postFile(String filesEndpoint, String content) throws MalformedURLException {
    return TestUtils.postFileWithFilename(client(), new URL(HTTP_APP_HOST + filesEndpoint), "test.txt", content.getBytes());
  }

  private String getVersions(String fileId) {
    var url = String.format("%s/files/%s/versions", APP_HOST, fileId);
    return getByUrl(url);
  }

  private String getIndexedFile(String fileId) {
    var url = ES_HOST + "/files/_doc/" + fileId;
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
