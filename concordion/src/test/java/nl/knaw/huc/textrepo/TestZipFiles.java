package nl.knaw.huc.textrepo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.textrepo.TestUtils.getResourceFileBits;

public class TestZipFiles extends AbstractConcordionTest {

  public static class TestZipFilesResult {
    public int status;
    public String locationCount;

    public String filename1;
    public String location1;
    public String fileIdIsUUID1;
    public String fileId1;

    public String filename2;
    public String location2;
    public String fileIdIsUUID2;
    public String fileId2;

  }

  public static class VersionsResult {
    public String fileHash1;
    public String fileHash2;

  }

  public static class EsFilesResult {
    public String getIndexedFile1;
    public String getIndexedFile2;
  }

  public TestZipFilesResult uploadZip(String filename) throws IOException {
    var zipFile = getResourceFileBits(filename);

    var response = postTestFile(zipFile, filename);
    var body = response.readEntity(String.class);

    var result = new TestZipFilesResult();
    result.status = response.getStatus();

    var locations = getLocationsMap(body);

    result.locationCount = "" + locations.keySet().size();

    result.filename1 = "een.txt";
    result.location1 = locations.get(result.filename1);
    result.fileId1 = TestUtils.getFileId(result.location1);
    result.fileIdIsUUID1 = TestUtils.isValidUUID(result.fileId1);

    result.filename2 = "twee.txt";
    result.location2 = locations.get(result.filename2);
    result.fileId2 = TestUtils.getFileId(result.location2);
    result.fileIdIsUUID2 = TestUtils.isValidUUID(result.fileId2);

    return result;
  }

  public VersionsResult requestLatestVersions(String fileId1, String fileId2) {
    var result = new VersionsResult();
    result.fileHash1 = getLatestVersionHash(fileId1);
    result.fileHash2 = getLatestVersionHash(fileId2);
    return result;
  }

  public EsFilesResult requestFilesFromEs(String fileId1, String fileId2) {
    var result = new EsFilesResult();
    result.getIndexedFile1 = getIndexedFile(fileId1);
    result.getIndexedFile2 = getIndexedFile(fileId2);
    return result;
  }

  private String getLatestVersionHash(String fileId) {
    var url = APP_HOST + "/files/" + fileId;
    return JsonPath.parse(getByUrl(url)).read("$.contentsSha");
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
        .name("contents")
        .fileName(filename)
        .size(bytes.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(contentDisposition, bytes, APPLICATION_OCTET_STREAM_TYPE));

    var filesEndpoint = APP_HOST + "/files";
    final var request = client()
        .register(MultiPartFeature.class)
        .target(filesEndpoint)
        .request();

    final var entity = Entity.entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }
}
