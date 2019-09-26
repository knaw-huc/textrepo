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
import static nl.knaw.huc.textrepo.TestUtils.replace;

public class TestMetadata extends AbstractConcordionTest {

  private static final String PUT_DOCUMENT_FILE_URL = HTTP_APP_HOST + "/documents/%s/files";

  public static class TestMetadataResult {
    public String documentId;
    public int addMetadataStatus;
    public String foo;
    public String spam;
    public String filename;

  }
  public static class TestUpdateFilenameResult {
    public String foo;
    public String spam;
    public String filename;
  }

  public static class UpdateMetadataEntryResult {
    public String foo;
    public String spam;
    public String filename;
  }

  public TestMetadataResult createDocumentWithMetadata(
      String filename,
      String documentEndpoint,
      String metadata,
      String metadataEndpoint
  ) throws MalformedURLException {
    var result = new TestMetadataResult();

    // create document
    var response = createDocument(filename, documentEndpoint);
    result.documentId = getDocumentId(getLocation(response).orElse(""));

    // add metadata
    var responseAddMetadata = addMetadata(result.documentId, metadata, metadataEndpoint);
    result.addMetadataStatus = responseAddMetadata.getStatus();

    // check metadata + filename
    var getMetadata = getMetadata(result.documentId);
    var getJson = getMetadata.readEntity(String.class);
    result.foo = JsonPath.parse(getJson).read("$.foo");
    result.spam = JsonPath.parse(getJson).read("$.spam");
    result.filename = JsonPath.parse(getJson).read("$.filename");

    return result;
  }

  public TestUpdateFilenameResult updateDocumentFilename(String documentFileEndpoint, String documentId, String newFilename) {
    var result = new TestUpdateFilenameResult();

    // update filename
    updateFilename(documentFileEndpoint, newFilename, documentId);

    // check updated filename
    var updatedMetadata = getMetadata(documentId);
    var updatedJson = updatedMetadata.readEntity(String.class);
    var parsed = JsonPath.parse(updatedJson);
    result.foo = parsed.read("$.foo");
    result.spam = parsed.read("$.spam");
    result.filename = parsed.read("$.filename");

    return result;
  }

  public TestUpdateFilenameResult updateMetadataEntry(
      String documentMetadataEndpoint,
      String documentId,
      String updatedKey,
      String updatedValue
  ) {
    var result = new TestUpdateFilenameResult();
    putMetadataEntry(documentMetadataEndpoint, documentId, updatedKey, updatedValue);

    var updatedMetadata = getMetadata(documentId);
    var updatedJson = updatedMetadata.readEntity(String.class);
    var parsed = JsonPath.parse(updatedJson);
    result.foo = parsed.read("$.foo");
    result.spam = parsed.read("$.spam");
    result.filename = parsed.read("$.filename");
    return result;
  }

  private Response createDocument(String filename, String documentsUrl) throws MalformedURLException {
    return postFileWithFilename(client(), new URL(HTTP_APP_HOST + documentsUrl), filename, "".getBytes());
  }

  private void updateFilename(String documentFileEndpoint, String newFilename, String documentId) {
    var url = HTTP_APP_HOST + documentFileEndpoint;
    putFileWithFilename(
        client(),
        replace(url, "documentId", documentId),
        newFilename,
        "content2".getBytes()
    );
  }

  private Response addMetadata(String documentId, String metadata, String metadataEndpoint) {
    var urlString = HTTP_APP_HOST + metadataEndpoint;
    var url = replace(urlString, "documentId", documentId);
    return client()
        .register(MultiPartFeature.class)
        .target(url)
        .request()
        .post(entity(metadata, APPLICATION_JSON));
  }

  private Response putMetadataEntry(
      String metadataEndpoint,
      String documentId,
      String key,
      String value
  ) {
    var urlString = HTTP_APP_HOST + metadataEndpoint;
    var url = replace(urlString, "documentId", documentId);
    url = replace(url, "key", key);

    return client()
        .register(MultiPartFeature.class)
        .target(url)
        .request()
        .put(entity(value, APPLICATION_JSON));
  }

  private Response getMetadata(String documentId) {
    return client()
        .register(MultiPartFeature.class)
        .target(format(HTTP_APP_HOST + "/documents/%s/metadata", documentId))
        .request().get();
  }
}
