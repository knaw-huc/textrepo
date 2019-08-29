package nl.knaw.huc.textrepo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

import static java.lang.String.format;
import static nl.knaw.huc.textrepo.Config.HTTP_APP_HOST;
import static nl.knaw.huc.textrepo.TestUtils.getDocumentId;
import static nl.knaw.huc.textrepo.TestUtils.getLocation;
import static nl.knaw.huc.textrepo.TestUtils.getMultiPartEntity;

public class TestVersions extends AbstractConcordionTest {

  private static final String DOCUMENTS_URL = HTTP_APP_HOST + "/documents";
  private static final String PUT_DOCUMENT_FILE_URL = HTTP_APP_HOST + "/documents/%s/files";

  public static class TestVersionsResult {
    public int status;
    public int statusUpdate;
    public String documentUuid;

    public String version1Sha;
    public String version2Sha;

    public String indexContentAfterUpdate;
  }

  public TestVersionsResult uploadMultipleVersions(String content, String newContent) {
    var result = new TestVersionsResult();

    var response = postFile(content);
    result.status = response.getStatus();
    result.documentUuid = getDocumentId(getLocation(response).orElse("/no-document-uuid"));

    result.statusUpdate = updateDocumentWithNewFile(result.documentUuid, newContent)
        .getStatus();

    var jsonVersions = getVersions(result.documentUuid);
    System.out.println("jsonVersions: " + jsonVersions);

    result.version1Sha = jsonPath.parse(jsonVersions).read("$[0].fileSha");
    result.version2Sha = jsonPath.parse(jsonVersions).read("$[1].fileSha");

    result.indexContentAfterUpdate = getIndexDocument(result.documentUuid);
    return result;
  }

  private Response postFile(String content) {
    var multiPart = new FormDataMultiPart().field("file", content);
    var request = client()
        .register(MultiPartFeature.class)
        .target(DOCUMENTS_URL)
        .request();
    return request.post(getMultiPartEntity(multiPart));
  }

  private Response updateDocumentWithNewFile(String documentUuid, String content) {
    var multiPart = new FormDataMultiPart().field("file", content);
    var request = client()
        .register(MultiPartFeature.class)
        .target(format(PUT_DOCUMENT_FILE_URL, documentUuid))
        .request();
    return request
        .put(getMultiPartEntity(multiPart));
  }

  private String getVersions(String documentId) {
    var url = APP_HOST + "/documents/" + documentId + "/versions";
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

  /**
   * Get locations map at $.locations with Jackson
   * since JsonPath doesn't like keys with dots.
   */
  private Map<String, String> getLocationsMap(String body) throws IOException {
    var mapper = new ObjectMapper();
    var root = mapper.readTree(body);
    var locationsNode = root.path("locations");
    return mapper.convertValue(locationsNode, new TypeReference<Map<String, String>>() {
    });
  }


}
