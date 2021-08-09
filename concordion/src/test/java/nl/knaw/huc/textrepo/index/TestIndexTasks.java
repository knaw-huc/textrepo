package nl.knaw.huc.textrepo.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import nl.knaw.huc.textrepo.AbstractConcordionTest;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL;
import static com.jayway.jsonpath.Option.SUPPRESS_EXCEPTIONS;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.textrepo.Config.FILE_INDEX;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.Config.TEXT_TYPE;
import static nl.knaw.huc.textrepo.util.IndexUtils.indexToUrl;
import static nl.knaw.huc.textrepo.util.TestResourceUtils.getResourceAsString;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.sleepMs;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public class TestIndexTasks extends AbstractConcordionTest {

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

  private final String searchAll = getResourceAsString("es-queries/search-all.json");
  String externalId = "test-" + randomAlphanumeric(10);

  public TestIndexTasks() throws IOException {
  }

  public String getEsQuery() {
    return asPrettyJson(this.searchAll);
  }

  public String getExternalId() {
    return this.externalId;
  }

  public static class ImportResult {
    public String status;
    public String body;
  }

  public static class StatusAndBodyResult {
    public int status;
    public String body;
  }

  public static class FileIndexResult {
    public int status;
    public String body;
    public int count;
  }

  public ImportResult importDoc() {
    var results = new ArrayList<Integer>();

    var response = requestImportDocumentTask(
        externalId,
        "het haasje".getBytes(),
        TEXT_TYPE
    );
    results.add(response.getStatus());
    var body = response.readEntity(String.class);

    var result = new ImportResult();
    result.status = results.stream().map(String::valueOf).collect(Collectors.joining(", "));
    result.body = asPrettyJson(body.equals("") ? " " : body);
    return result;
  }

  private Response requestImportDocumentTask(String externalId, byte[] content, String type) {
    var contentDisposition = FormDataContentDisposition
        .name("contents")
        .fileName(externalId + ".txt")
        .size(content.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(
            contentDisposition,
            new String(content, UTF_8),
            APPLICATION_OCTET_STREAM_TYPE)
        );

    var importUrl = HOST + "/task/import/documents/" + externalId + "/" + type
        + "?allowNewDocument=true"
        + "&index=false";
    var request = client
        .register(MultiPartFeature.class)
        .target(importUrl)
        .request();

    var entity = entity(multiPart, multiPart.getMediaType());
    return request.post(entity);
  }

  public FileIndexResult searchFileIndex() {
    // Wait for indexing:
    sleepMs(1000);

    var result = new FileIndexResult();
    var response = searchFileIndex(this.searchAll);
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var found = jsonPath.parse(body).read("$.hits.hits[*]._source.file.id", new TypeRef<List<String>>() {});

    result.count = found.size();
    return result;
  }

  public static Response searchFileIndex(String query) {
    return client()
        .register(MultiPartFeature.class)
        .target(indexToUrl(FILE_INDEX) + "/_search")
        .request()
        .post(entity(query, APPLICATION_JSON_TYPE));
  }

  public static StatusAndBodyResult indexType(String indexTaskEndpoint, String typeName) {
    var indexTaskUrl = HOST + indexTaskEndpoint
        .replace("{name}", typeName);

    var request = client
        .target(indexTaskUrl)
        .request()
        .post(Entity.json(null));


    var result = new StatusAndBodyResult();
    result.body = request.readEntity(String.class);
    result.status = request.getStatus();
    return result;
  }

  public static StatusAndBodyResult deleteDoc(String deleteDocTaskEndpoint, String externalId) {
    var indexTaskUrl = HOST + deleteDocTaskEndpoint
        .replace("{externalId}", externalId);

    var request = client
        .target(indexTaskUrl)
        .request()
        .delete();

    var result = new StatusAndBodyResult();
    result.body = request.readEntity(String.class);
    result.status = request.getStatus();
    return result;
  }

  public static StatusAndBodyResult deleteOrphaned(String deleteOrphanedTaskEndpoint) {
    var indexTaskUrl = HOST + deleteOrphanedTaskEndpoint;

    var request = client
        .target(indexTaskUrl)
        .request()
        .delete();

    var result = new StatusAndBodyResult();
    result.body = request.readEntity(String.class);
    result.status = request.getStatus();
    return result;
  }



}
