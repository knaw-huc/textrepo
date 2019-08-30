package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.JsonPath;

import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.String.format;
import static nl.knaw.huc.textrepo.Config.HTTP_APP_HOST;
import static nl.knaw.huc.textrepo.TestUtils.getDocumentId;
import static nl.knaw.huc.textrepo.TestUtils.getLocation;
import static nl.knaw.huc.textrepo.TestUtils.putFileWithFilename;

public class TestVersions extends AbstractConcordionTest {

  private final String DOCUMENTS_URL = HTTP_APP_HOST + "/documents";
  private static final String PUT_DOCUMENT_FILE_URL = HTTP_APP_HOST + "/documents/%s/files";

  public static class TestVersionsResult {
    public int status;
    public int statusUpdate;
    public String documentUuid;
    public String version1Sha;
    public String version2Sha;
    public String indexContentAfterUpdate;
  }

  public TestVersionsResult uploadMultipleVersions(
      String content, String newContent
  ) throws MalformedURLException {
    var result = new TestVersionsResult();

    var response = postFile(content);

    result.status = response.getStatus();
    result.documentUuid = getDocumentId(getLocation(response).orElse("/no-document-uuid"));

    result.statusUpdate = updateDocument(newContent, result);

    var jsonVersions = getVersions(result.documentUuid);

    result.version1Sha = jsonPath.parse(jsonVersions).read("$[0].fileSha");
    result.version2Sha = jsonPath.parse(jsonVersions).read("$[1].fileSha");

    result.indexContentAfterUpdate = getIndexDocument(result.documentUuid);
    return result;
  }

  private int updateDocument(String newContent, TestVersionsResult result) {
    return putFileWithFilename(
        client(),
        format(PUT_DOCUMENT_FILE_URL, result.documentUuid),
        "test.txt",
        newContent.getBytes()
    ).getStatus();
  }

  private Response postFile(String content) throws MalformedURLException {
    return TestUtils.postFileWithFilename(client(), new URL(DOCUMENTS_URL), "test.txt", content.getBytes());
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
