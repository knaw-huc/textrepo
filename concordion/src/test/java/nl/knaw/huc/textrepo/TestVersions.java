package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.JsonPath;
import nl.knaw.huc.textrepo.util.TestUtils;

import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;

import static nl.knaw.huc.textrepo.Config.HTTP_APP_HOST;
import static nl.knaw.huc.textrepo.util.TestUtils.getFileId;
import static nl.knaw.huc.textrepo.util.TestUtils.getLocation;
import static nl.knaw.huc.textrepo.util.TestUtils.putFileWithFilename;
import static nl.knaw.huc.textrepo.util.TestUtils.replace;

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
      String contents,
      String filesEndpoint,
      String newContent,
      String fileContentsEndpoint
  ) throws MalformedURLException {
    var result = new TestVersionsResult();

    var response = postFile(filesEndpoint, contents);

    result.status = response.getStatus();
    result.fileId = getFileId(getLocation(response).orElse("/no-file-uuid"));

    result.statusUpdate = updateFile(fileContentsEndpoint, newContent, result);

    var jsonVersions = getVersions(result.fileId);

    result.version1Sha = jsonPath.parse(jsonVersions).read("$[0].contentsSha");
    result.version2Sha = jsonPath.parse(jsonVersions).read("$[1].contentsSha");

    result.indexContentAfterUpdate = getIndexedFile(result.fileId);
    return result;
  }

  private int updateFile(String fileContentsEndpoint, String newContent, TestVersionsResult result)
      throws MalformedURLException {
    return putFileWithFilename(
        client(),
        new URL(replace(HTTP_APP_HOST + fileContentsEndpoint, "fileId", result.fileId)),
        "test.txt",
        newContent.getBytes()
    ).getStatus();
  }

  private Response postFile(String filesEndpoint, String contents) throws MalformedURLException {
    return TestUtils.postFileWithFilename(
        client(),
        new URL(HTTP_APP_HOST + filesEndpoint),
        "test.txt",
        contents.getBytes()
    );
  }

  private String getVersions(String fileId) {
    var url = String.format("%s/rest/files/%s/versions", APP_HOST, fileId);
    return getByUrl(url);
  }

  private String getIndexedFile(String fileId) {
    var url = ES_HOST + "/files/_doc/" + fileId;
    return JsonPath.parse(getByUrl(url)).read("$._source.contents");
  }

  private String getByUrl(String url) {
    return client()
        .target(url)
        .request()
        .get()
        .readEntity(String.class);
  }

}
