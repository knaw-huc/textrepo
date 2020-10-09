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
  private String file1;
  private String searchByDocMetadata;

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

  public String createDocuments() {
    this.doc1 = RestUtils.createDocument("dummy-" + randomAlphabetic(5));
    RestUtils.createDocumentMetadata(doc1, "foo", "bar");

    var doc2 = RestUtils.createDocument("dummy-" + randomAlphabetic(5));
    RestUtils.createDocumentMetadata(doc2, "spam", "eggs");

    return toJson(asList(doc1, doc2));
  }

  public String createFiles(String docIds) {
    var fileIds = new ArrayList<String>();
    fromJson(docIds).forEach(
        (docId) -> fileIds.add(RestUtils.createFile(docId, textTypeId))
    );
    this.file1 = fileIds.get(0);
    return toJson(fileIds);
  }

  public String createVersions(String fileIds) {
    var versionIds = new ArrayList<String>();
    fromJson(fileIds).forEach(
        (fileId) -> versionIds.add(RestUtils.createVersion(fileId, "some perturbing content"))
    );
    return toJson(versionIds);
  }

  public String getEsQuerySearchByDocMetadata() throws IOException {
    searchByDocMetadata = getResourceAsString("es-queries/search-by-doc-metadata.json");
    return asPrettyJson(searchByDocMetadata);
  }

  public static class DocMetadataResult {
    public int status;
    public String body;
    public String found;
  }

  public DocMetadataResult searchEsQuerySearchByDocMetadata(
      String docMetaKey,
      String docMetaValue
  ) {
    sleepMs(5000);
    var result = new DocMetadataResult();
    var query = this.searchByDocMetadata
        .replace("{key}", docMetaKey)
        .replace("{value}", docMetaValue);
    var response = searchEs(query);
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var found = jsonPath.parse(body).read("$.hits.hits[*]._source.doc.id", new TypeRef<List<String>>() {});
    result.found = found.size() == 1 && found.contains(doc1)
        ? "correct file"
        : format("expected %s but got %s", doc1, Arrays.toString(found.toArray()));
    return result;
  }


  private String toJson(List<String> items) {
    try {
      return new ObjectMapper().writeValueAsString(items);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Could not create json from: " + Arrays.toString(items.toArray()));
    }
  }

  private List<String> fromJson(String items) {
    try {
      return asList(new ObjectMapper().readValue(items, String[].class));
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Could not create list from: " + items);
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


}
