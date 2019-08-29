package nl.knaw.huc.textrepo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.Ignore;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.textrepo.TestUtils.getResourceFileBits;

public class TestZipDocuments extends AbstractConcordionTest {

  public static class TestZipDocumentsResult {
    public int status;
    public String locationCount;

    public String filename1;
    public String location1;
    public String documentIdIsUUID1;
    public String documentId1;

    public String filename2;
    public String location2;
    public String documentIdIsUUID2;
    public String documentId2;

    public String fileHash1;
    public String fileHash2;

    public String getIndexDocument1;
    public String getIndexDocument2;
  }

  public TestZipDocumentsResult uploadZip(String filename) throws IOException {
    var zipFile = getResourceFileBits(filename);

    var response = postTestFile(zipFile, filename);
    var body = response.readEntity(String.class);

    var result = new TestZipDocumentsResult();
    result.status = response.getStatus();

    var locations = getLocationsMap(body);

    result.locationCount = "" + locations.keySet().size();

    result.filename1 = "een.txt";
    result.location1 = locations.get(result.filename1);
    result.documentId1 = TestUtils.getDocumentId(result.location1);
    result.documentIdIsUUID1 = TestUtils.isValidUUID(result.documentId1);

    result.filename2 = "twee.txt";
    result.location2 = locations.get(result.filename2);
    result.documentId2 = TestUtils.getDocumentId(result.location2);
    result.documentIdIsUUID2 = TestUtils.isValidUUID(result.documentId2);

    result.fileHash1 = getLatestVersionHash(result.documentId1);
    result.fileHash2 = getLatestVersionHash(result.documentId2);

    result.getIndexDocument1 = getIndexDocument(result.documentId1);
    result.getIndexDocument2 = getIndexDocument(result.documentId2);

    return result;
  }

  private String getLatestVersionHash(String documentId) {
    var url = APP_HOST + "/documents/" + documentId;
    return JsonPath.parse(getByUrl(url)).read("$.fileSha");
  }

  private String getIndexDocument(String documentId) {
    String url = ES_HOST + "/documents/_doc/" + documentId;
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
    return mapper.convertValue(locationsNode, new TypeReference<Map<String, String>>() {});
  }

  private Response postTestFile(byte[] bytes, String filename) {
    var contentDisposition = FormDataContentDisposition
        .name("file")
        .fileName(filename)
        .size(bytes.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(contentDisposition, bytes, APPLICATION_OCTET_STREAM_TYPE));

    var documentsEndpoint = APP_HOST + "/documents";
    final var request = client()
        .register(MultiPartFeature.class)
        .target(documentsEndpoint)
        .request();

    final var entity = Entity.entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }

}
