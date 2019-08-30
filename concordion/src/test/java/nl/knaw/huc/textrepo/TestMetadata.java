package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.JsonPath;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.core.Response;

import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huc.textrepo.Config.HTTP_APP_HOST;
import static nl.knaw.huc.textrepo.TestUtils.getDocumentId;
import static nl.knaw.huc.textrepo.TestUtils.getLocation;
import static nl.knaw.huc.textrepo.TestUtils.postFileWithFilename;
import static nl.knaw.huc.textrepo.TestUtils.putFileWithFilename;

public class TestMetadata extends AbstractConcordionTest {

  private final String DOCUMENTS_URL = HTTP_APP_HOST + "/documents";
  private static final String PUT_DOCUMENT_FILE_URL = HTTP_APP_HOST + "/documents/%s/files";
  private static final String METADATA_URL = HTTP_APP_HOST + "/documents/%s/metadata";

  public static class TestMetadataResult {
    public String documentId;
    public int addMetadataStatus;
    public String foo;
    public String spam;
    public String filename;
    public String stillFoo;
    public String stillSpam;
    public String updatedFilename;
  }

  public TestMetadataResult createAndUpdateMetadata(String filename, String metadata, String newFilename)
      throws MalformedURLException {
    var result = new TestMetadataResult();

    // create document
    var response = createDocument(filename);
    result.documentId = getDocumentId(getLocation(response).orElse(""));

    // add metadata
    var responseAddMetadata = addMetadata(result.documentId, metadata);
    result.addMetadataStatus = responseAddMetadata.getStatus();

    // check metadata + filename
    var getMetadata = getMetadata(result.documentId);
    var getJson = getMetadata.readEntity(String.class);
    result.foo = JsonPath.parse(getJson).read("$.foo");
    result.spam = JsonPath.parse(getJson).read("$.spam");
    result.filename = JsonPath.parse(getJson).read("$.filename");

    // update filename
    updateFilename(newFilename, result.documentId);

    // check updated filename
    var updatedMetadata = getMetadata(result.documentId);
    var updatedJson = updatedMetadata.readEntity(String.class);
    result.stillFoo = JsonPath.parse(updatedJson).read("$.foo");
    result.stillSpam = JsonPath.parse(updatedJson).read("$.spam");
    result.updatedFilename = JsonPath.parse(updatedJson).read("$.filename");

    return result;
  }

  private Response createDocument(String filename) throws MalformedURLException {
    return postFileWithFilename(client(), new URL(DOCUMENTS_URL), filename, "".getBytes());
  }

  private void updateFilename(String newFilename, String documentId) {
    putFileWithFilename(
        client(),
        format(PUT_DOCUMENT_FILE_URL, documentId),
        newFilename,
        "content2".getBytes()
    );
  }

  private Response addMetadata(String documentId, String metadata) {
    var url = format(METADATA_URL, documentId);
    return client()
        .register(MultiPartFeature.class)
        .target(url)
        .request().post(entity(metadata, APPLICATION_JSON));
  }

  private Response getMetadata(String documentId) {
    return client()
        .register(MultiPartFeature.class)
        .target(format(METADATA_URL, documentId))
        .request().get();
  }

}
