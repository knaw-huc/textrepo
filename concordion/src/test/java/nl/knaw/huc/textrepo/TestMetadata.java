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
import static nl.knaw.huc.textrepo.util.TestUtils.getFileId;
import static nl.knaw.huc.textrepo.util.TestUtils.getLocation;
import static nl.knaw.huc.textrepo.util.TestUtils.postFileWithFilename;
import static nl.knaw.huc.textrepo.util.TestUtils.putFileWithFilename;
import static nl.knaw.huc.textrepo.util.TestUtils.replace;

public class TestMetadata extends AbstractConcordionTest {

  public static class TestMetadataResult {
    public String fileId;
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

  public TestMetadataResult createFileWithMetadata(
      String filename,
      String fileEndpoint,
      String metadata,
      String metadataEndpoint
  ) throws MalformedURLException {
    var result = new TestMetadataResult();

    // create file
    var response = createFile(filename, fileEndpoint);
    result.fileId = getFileId(getLocation(response).orElse(""));

    // add metadata
    var responseAddMetadata = addMetadata(result.fileId, metadata, metadataEndpoint);
    result.addMetadataStatus = responseAddMetadata.getStatus();

    // check metadata + filename
    var getMetadata = getMetadata(result.fileId);
    var getJson = getMetadata.readEntity(String.class);
    result.foo = JsonPath.parse(getJson).read("$.foo");
    result.spam = JsonPath.parse(getJson).read("$.spam");
    result.filename = JsonPath.parse(getJson).read("$.filename");

    return result;
  }

  public TestUpdateFilenameResult updateMetadataNameOfFile(String fileContentsEndpoint, String fileId, String newFilename)
      throws MalformedURLException {
    var result = new TestUpdateFilenameResult();

    // update filename
    updateFilename(fileContentsEndpoint, newFilename, fileId);

    // check updated filename
    var updatedMetadata = getMetadata(fileId);
    var updatedJson = updatedMetadata.readEntity(String.class);
    var parsed = JsonPath.parse(updatedJson);
    result.foo = parsed.read("$.foo");
    result.spam = parsed.read("$.spam");
    result.filename = parsed.read("$.filename");

    return result;
  }

  public TestUpdateFilenameResult updateMetadataEntry(
      String fileMetadataEndpoint,
      String fileId,
      String updatedKey,
      String updatedValue
  ) {
    var result = new TestUpdateFilenameResult();
    putMetadataEntry(fileMetadataEndpoint, fileId, updatedKey, updatedValue);

    var updatedMetadata = getMetadata(fileId);
    var updatedJson = updatedMetadata.readEntity(String.class);
    var parsed = JsonPath.parse(updatedJson);
    result.foo = parsed.read("$.foo");
    result.spam = parsed.read("$.spam");
    result.filename = parsed.read("$.filename");
    return result;
  }

  private Response createFile(String filename, String filesUrl) throws MalformedURLException {
    return postFileWithFilename(client(), new URL(HTTP_APP_HOST + filesUrl), filename, "".getBytes());
  }

  private void updateFilename(String fileContentsEndpoint, String newFilename, String fileId)
      throws MalformedURLException {
    var url = HTTP_APP_HOST + fileContentsEndpoint;
    putFileWithFilename(
        client(),
        new URL(replace(url, "fileId", fileId)),
        newFilename,
        "content2".getBytes()
    );
  }

  private Response addMetadata(String fileId, String metadata, String metadataEndpoint) {
    var urlString = HTTP_APP_HOST + metadataEndpoint;
    var url = replace(urlString, "fileId", fileId);
    return client()
        .register(MultiPartFeature.class)
        .target(url)
        .request()
        .post(entity(metadata, APPLICATION_JSON));
  }

  private Response putMetadataEntry(
      String metadataEndpoint,
      String fileId,
      String key,
      String value
  ) {
    var urlString = HTTP_APP_HOST + metadataEndpoint;
    var url = replace(urlString, "fileId", fileId);
    url = replace(url, "key", key);

    return client()
        .register(MultiPartFeature.class)
        .target(url)
        .request()
        .put(entity(value, APPLICATION_JSON));
  }

  private Response getMetadata(String fileId) {
    return client()
        .register(MultiPartFeature.class)
        .target(format(HTTP_APP_HOST + "/rest/files/%s/metadata", fileId))
        .request().get();
  }
}
