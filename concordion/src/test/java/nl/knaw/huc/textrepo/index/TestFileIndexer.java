package nl.knaw.huc.textrepo.index;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL;
import static com.jayway.jsonpath.Option.SUPPRESS_EXCEPTIONS;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.asList;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static nl.knaw.huc.textrepo.Config.FILE_INDEX;
import static nl.knaw.huc.textrepo.util.IndexUtils.indexToUrl;
import static nl.knaw.huc.textrepo.util.TestResourceUtils.getResourceAsString;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.sleepMs;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class TestFileIndexer extends AbstractConcordionTest {

  private String doc1;
  private String doc2;
  private String file1;
  private String file2;
  private String dateTime;

  private final String searchByFileType = getResourceAsString("es-queries/search-by-file-type.json");
  private final String searchByDocMetadata = getResourceAsString("es-queries/search-by-doc-metadata.json");
  private final String searchByContentsLastModified =
      getResourceAsString("es-queries/search-by-contents-last-modified.json");
  private final String searchByFileTypeAndContentsLastModified =
      getResourceAsString("es-queries/search-by-file-type-and-contents-modified.json");
  private final String searchDocsByMetadata =
      getResourceAsString("es-queries/search-docs-by-metadata.json");
  private final String searchByFileMetadataKey =
      getResourceAsString("es-queries/search-by-file-metadata-key.json");

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

  public TestFileIndexer() throws IOException {
  }

  public String createDocuments() {
    this.doc1 = RestUtils.createDocument("dummy-" + randomAlphabetic(5));
    RestUtils.createDocumentMetadata(doc1, "foo", "bar");

    this.doc2 = RestUtils.createDocument("dummy-" + randomAlphabetic(5));
    RestUtils.createDocumentMetadata(doc2, "spam", "eggs");

    return toJson(asList(doc1, doc2));
  }

  public String createFiles() {
    // file 1 is of type foo:
    file1 = RestUtils.createFile(doc1, fooTypeId);
    RestUtils.createFileMetadata(file1, "file-foo", "bar");

    // file 1 is of type text:
    file2 = RestUtils.createFile(doc2, textTypeId);
    RestUtils.createFileMetadata(file2, "file-foo", "bas");

    return toJson(asList(file1, file2));
  }

  public String createVersions() {
    var versionIds = new ArrayList<String>();

    var sameContent = "some perturbing content";
    versionIds.add(RestUtils.createVersion(file1, sameContent));
    versionIds.add(RestUtils.createVersion(file1, sameContent));

    var firstVersion = RestUtils.createVersion(file2, "some irksome content");
    versionIds.add(firstVersion);

    // add some timeout between first and second version of second file:
    sleepMs(1000);
    this.dateTime = ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(now());

    var secondversion = RestUtils.createVersion(file2, "some friable content");
    versionIds.add(secondversion);

    // make sure es has finished indexing:
    sleepMs(1000);

    return toJson(versionIds);
  }

  public String getEsQuerySearchByFileType() {
    return asPrettyJson(searchByFileType);
  }

  public String getEsQuerySearchByDocMetadata() {
    return asPrettyJson(searchByDocMetadata);
  }

  public String getEsQuerySearchByContentsLastModified() {
    return asPrettyJson(searchByContentsLastModified);
  }

  public String getDateTime() {
    return dateTime;
  }

  public static class DocMetadataResult {
    public int status;
    public String body;
    public String found;
  }
  public DocMetadataResult searchEsQuerySearchByFileType(
      String fileType
  ) {
    var result = new DocMetadataResult();
    var query = this.searchByFileType
        .replace("{type}", fileType);
    var response = searchEs(query);
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var found = jsonPath.parse(body).read("$.hits.hits[*]._source.file.id", new TypeRef<List<String>>() {});
    result.found = found.size() == 1 && found.contains(file2)
        ? "correct file"
        : format("expected %s but got %s", file2, Arrays.toString(found.toArray()));
    return result;
  }

  public DocMetadataResult searchEsQuerySearchByDocMetadata(
      String docMetaKey,
      String docMetaValue
  ) {
    var result = new DocMetadataResult();
    var query = this.searchByDocMetadata
        .replace("{key}", docMetaKey)
        .replace("{value}", docMetaValue);
    var response = searchEs(query);
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var found = jsonPath.parse(body).read("$.hits.hits[*]._source.file.id", new TypeRef<List<String>>() {});
    result.found = found.size() == 1 && found.contains(file1)
        ? "correct file"
        : format("expected %s but got %s", file1, Arrays.toString(found.toArray()));
    return result;
  }

  public String getEsQuerySearchByFileMetadataKey() {
    return asPrettyJson(searchByFileMetadataKey);
  }

  public DocMetadataResult searchEsQuerySearchByFileMetadataKey(
      String fileMetaKey
  ) {
    var result = new DocMetadataResult();
    var query = this.searchByFileMetadataKey
        .replace("{key}", fileMetaKey);

    var response = searchEs(query);
    result.status = response.getStatus();
    var body = response.readEntity(String.class);

    result.body = asPrettyJson(body);

    var found = jsonPath.parse(body).read("$.hits.hits[*]._source.file.id", new TypeRef<List<String>>() {});
    result.found = found.size() == 2 && found.contains(file1) && found.contains(file2)
        ? "correct files"
        : format("expected %s but got %s", Arrays.toString(new String[]{file1, file2}), Arrays.toString(found.toArray()));

    return result;
  }

  public DocMetadataResult searchEsQuerySearchByContentsLastModified() {
    var result = new DocMetadataResult();
    var query = this.searchByContentsLastModified
        .replace("{dateTime}", this.getDateTime());
    var response = searchEs(query);
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var found = jsonPath.parse(body).read("$.hits.hits[*]._source.file.id", new TypeRef<List<String>>() {});
    result.found = found.size() == 1 && found.contains(file2)
        ? "correct file"
        : format("expected %s but got %s", file2, Arrays.toString(found.toArray()));
    return result;
  }

  private String toJson(List<String> items) {
    try {
      return new ObjectMapper().writeValueAsString(items);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Could not create json from: " + Arrays.toString(items.toArray()));
    }
  }

  /**
   * @return String document uuid
   */
  public static Response searchEs(String query) {
    return client()
        .register(MultiPartFeature.class)
        .target(indexToUrl(FILE_INDEX) + "/_search")
        .request()
        .post(entity(query, APPLICATION_JSON_TYPE));
  }

  public String getEsQuerySearchByFileTypeAndContentsLastModified() throws IOException {
    return asPrettyJson(searchByFileTypeAndContentsLastModified);
  }

  public DocMetadataResult searchEsQuerySearchByFileTypeAndContentsLastModified(
      String type
  ) {
    var result = new DocMetadataResult();
    var query = this.searchByFileTypeAndContentsLastModified
        .replace("{dateTime}", this.getDateTime())
        .replace("{type}", type);

    var response = searchEs(query);
    result.status = response.getStatus();
    var body = response.readEntity(String.class);

    result.body = asPrettyJson(body);

    var found = jsonPath.parse(body).read("$.hits.hits[*]._source.file.id", new TypeRef<List<String>>() {});
    result.found = found.size() == 1 && found.contains(file2)
        ? "correct file"
        : format("expected %s but got %s", file2, Arrays.toString(found.toArray()));

    return result;
  }

  public String getEsQuerySearchDocsByMetadata() throws IOException {
    return asPrettyJson(searchDocsByMetadata);
  }

  public DocMetadataResult searchEsQueryDocsByMetadata(
      String docMetaKey,
      String docMetaValue
  ) {
    var result = new DocMetadataResult();
    var query = this.searchDocsByMetadata
        .replace("{key}", docMetaKey)
        .replace("{value}", docMetaValue);

    var response = searchEs(query);
    result.status = response.getStatus();
    var body = response.readEntity(String.class);

    result.body = asPrettyJson(body);

    var found = jsonPath.parse(body).read("$.hits.hits[*]._source.doc.id", new TypeRef<List<String>>() {});
    result.found = found.size() == 1 && found.contains(doc1)
        ? "correct document"
        : format("expected %s but got %s", doc1, Arrays.toString(found.toArray()));

    return result;
  }

}
