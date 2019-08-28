package nl.knaw.huc.textrepo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.textrepo.Config.HTTP_APP_HOST;
import static nl.knaw.huc.textrepo.TestUtils.getResourceFileBits;

public class TestZipDocuments extends AbstractConcordionTest {
  private static final String HOST = HTTP_APP_HOST;

  public static class TestZipDocumentsResult {
    public int status;
    public String locationCount;
    public String filename1;
    public String location1;
    public String documentIdIsUUID1;
    public String documentId1;
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
    return result;
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
    System.out.println("documentsEndpoint: " + documentsEndpoint);
    final var request = client()
        .register(MultiPartFeature.class)
        .target(documentsEndpoint)
        .request();

    final var entity = Entity.entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }

}
