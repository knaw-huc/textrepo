package nl.knaw.huc.textrepo.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL;
import static com.jayway.jsonpath.Option.SUPPRESS_EXCEPTIONS;
import static java.lang.String.format;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static nl.knaw.huc.textrepo.Config.FILE_INDEX;
import static nl.knaw.huc.textrepo.Config.TEXT_TYPE;
import static nl.knaw.huc.textrepo.util.IndexUtils.indexToUrl;
import static nl.knaw.huc.textrepo.util.TestResourceUtils.getResourceAsString;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.isValidUuid;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceUrlParams;
import static nl.knaw.huc.textrepo.util.TestUtils.sleepMs;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class TestIndexMutations extends AbstractConcordionTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Test uses JsonPath with a TypeRef, so we need to configure a Jackson ObjectMapper:
   */
  private final ParseContext jsonPath = JsonPath.using(Configuration
      .defaultConfiguration()
      .addOptions(DEFAULT_PATH_LEAF_TO_NULL)
      .addOptions(SUPPRESS_EXCEPTIONS)
      .mappingProvider(new JacksonMappingProvider(objectMapper))
      .jsonProvider(new JacksonJsonNodeJsonProvider(objectMapper)));

  private final String searchByFileType = getResourceAsString("es-queries/search-by-file-type.json");
  private final String searchAll = getResourceAsString("es-queries/search-all.json");

  private String fileId;

  public TestIndexMutations() throws IOException {
  }

  public String createDocument() {
    return RestUtils.createDocument("test-" + randomAlphabetic(5));
  }

  public String createFile(String docId) {
    this.fileId = RestUtils.createFile(docId, textTypeId);
    return fileId;
  }

  public FileIndexResult searchFileIndexWithoutVersions() {
    // Wait for indexing:
    sleepMs(1000);

    var result = new FileIndexResult();
    var query = this.searchByFileType
        .replace("{type}", TEXT_TYPE);
    var response = searchFileIndex(query);
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var found = jsonPath.parse(body).read("$.hits.hits[*]._source.file.id", new TypeRef<List<String>>() {
    });
    result.found = found.size() == 1 && found.contains(fileId)
        ? "correct file"
        : format("expected %s but got %s", fileId, Arrays.toString(found.toArray()));

    result.versionCount = jsonPath
        .parse(body)
        .read("$.hits.hits[*]._source.versions[*]", new TypeRef<List<Object>>() {
        })
        .size();
    return result;
  }

  public static class UploadResult {
    public String versionUuid1;
    public String versionUuid2;
    public String validVersions;
  }

  public UploadResult upload(
      String fileId1, String content1
  ) {
    var result = new UploadResult();
    result.versionUuid1 = RestUtils.createVersion(fileId1, content1);
    result.versionUuid2 = RestUtils.createVersion(fileId1, content1);
    result.validVersions = isValidUuid(result.versionUuid2)
        ? "valid versions"
        : "one or more invalid version UUIDs";
    return result;
  }

  public static class FileIndexResult {
    public int status;
    public String body;
    public String found;
    public int versionCount;
    public String type;
  }

  public FileIndexResult searchFileIndexWithVersions() {
    // Wait for indexing:
    sleepMs(1000);

    var result = new FileIndexResult();
    var query = this.searchByFileType
        .replace("{type}", TEXT_TYPE);
    var response = searchFileIndex(query);
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var found = jsonPath.parse(body).read("$.hits.hits[*]._source.file.id", new TypeRef<List<String>>() {
    });

    result.found = found.size() == 1 && found.contains(fileId)
        ? "correct file"
        : format("expected %s but got %s", fileId, Arrays.toString(found.toArray()));

    result.versionCount = jsonPath
        .parse(body)
        .read("$.hits.hits[*]._source.versions[*]", new TypeRef<List<Object>>() {
        })
        .size();

    return result;
  }

  public static class UpdateResult {
    public int status;
    public String body;
    public String updatedType;
  }

  public UpdateResult update(String endpoint, String id, String updatedEntity, String docId, int typeId) {
    updatedEntity = updatedEntity
        .replace("{docId}", docId)
        .replace("{typeId}", "" + typeId);

    final var response = client
        .target(replaceUrlParams(endpoint, id))
        .request()
        .put(entity(updatedEntity, APPLICATION_JSON_TYPE));

    var body = response.readEntity(String.class);
    var result = new UpdateResult();
    result.status = response.getStatus();
    result.body = asPrettyJson(body);
    var json = jsonPath.parse(body);
    var resultTypeId = json.read("$.typeId", Integer.class);
    result.updatedType = resultTypeId == fooTypeId ? "updated type" : "" + resultTypeId + " != " + typeId;
    return result;
  }

  public FileIndexResult searchFileIndexWithType(String type) {
    // Wait for indexing:
    sleepMs(1000);

    var result = new FileIndexResult();
    var query = this.searchAll
        .replace("{type}", type);
    var response = searchFileIndex(query);
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var found = jsonPath.parse(body).read("$.hits.hits[*]._source.file.id", new TypeRef<List<String>>() {});
    result.found = found.size() == 1 && found.contains(fileId)
        ? "correct file"
        : format("expected %s but got %s", fileId, Arrays.toString(found.toArray()));

    result.versionCount = jsonPath
        .parse(body)
        .read("$.hits.hits[*]._source.versions[*]", new TypeRef<List<Object>>() {
        })
        .size();
    var foundType = jsonPath
        .parse(body)
        .read("$.hits.hits[0]._source.file.type.name", String.class);
    result.type = foundType.equals(type)
        ? "correct type"
        : format("expected %s but got %s", type, foundType);

    return result;
  }

  /**
   * @return String document uuid
   */
  public static Response searchFileIndex(String query) {
    return client()
        .register(MultiPartFeature.class)
        .target(indexToUrl(FILE_INDEX) + "/_search")
        .request()
        .post(entity(query, APPLICATION_JSON_TYPE));
  }

  public String getEsQuery() {
    return asPrettyJson(this.searchByFileType);
  }

}
