package nl.knaw.huc.resources;

import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Type;
import nl.knaw.huc.service.type.TypeService;
import nl.knaw.huc.service.index.MappedIndexerConfiguration;
import nl.knaw.huc.service.index.IndexerException;
import nl.knaw.huc.service.index.MappedIndexer;
import nl.knaw.huc.service.index.ElasticsearchConfiguration;
import nl.knaw.huc.service.index.FieldsConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpRequest;

import java.io.IOException;
import java.util.UUID;

import static java.lang.String.format;
import static nl.knaw.huc.resources.TestUtils.getResourceAsString;
import static nl.knaw.huc.service.index.FieldsType.MULTIPART;
import static nl.knaw.huc.service.index.FieldsType.ORIGINAL;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonSchemaBody.jsonSchema;
import static org.mockserver.verify.VerificationTimes.once;

// TODO: merge with index/MappedIndexerTest.java
public class MappedIndexerTest {

  private static ClientAndServer mockServer;
  private static final int mockPort = 1080;
  private static final String mockMappingEndpoint = "/mock-mapping";
  private static final String mockFieldsEndpoint = "/mock-fields";
  private static final String mockTypesEndpoint = "/mock-types";

  private static ClientAndServer mockIndexServer;
  private static final int mockIndexPort = 1081;
  private final Type testType = new Type("test-type", "test/mimetype");
  private final TypeService typeServiceMock = mock(TypeService.class);

  @BeforeAll
  public static void setUpClass() {
    mockServer = ClientAndServer.startClientAndServer(mockPort);
    mockIndexServer = ClientAndServer.startClientAndServer(mockIndexPort);
  }

  @BeforeEach
  public void before() {
    mockServer.reset();
    mockIndexServer.reset();
    MockitoAnnotations.initMocks(this);
    when(typeServiceMock.getType(any())).thenReturn(testType);
  }

  @AfterEach
  public void resetMocks() {
    reset(typeServiceMock);
  }

  @AfterAll
  public static void tearDown() {
    mockServer.stop();
    mockIndexServer.stop();
  }

  @Test
  public void testInstantiationElasticCustomFacetIndexer_requestsMapping() throws IOException, IndexerException {
    var config = createCustomFacetIndexerConfiguration(ORIGINAL.getName());
    mockTypesEndpoint();
    var getMappingRequest = request()
        .withMethod("GET")
        .withPath(mockMappingEndpoint);
    mockGetEndpoint(getResourceAsString("indexer/test.mapping.json"), getMappingRequest);
    var putIndexRequest = request()
        .withMethod("PUT")
        .withPath("/" + config.elasticsearch.index)
        // because es client changes order of fields, verify using json schema:
        .withBody(jsonSchema(getResourceAsString("indexer/test.schema.json")));
    mockCreatingIndexResponse(config.elasticsearch.index, putIndexRequest);

    new MappedIndexer(config, typeServiceMock);

    mockServer.verify(getMappingRequest, once());
    mockIndexServer.verify(putIndexRequest, once());
  }

  @Test
  public void testIndexFile_requestsFields() throws IOException, IndexerException {
    var config = createCustomFacetIndexerConfiguration(ORIGINAL.getName());
    mockTypesEndpoint();
    mockMappingEndpoint();
    mockCreatingIndexResponse(config);
    var indexer = new MappedIndexer(config, typeServiceMock);
    var file = new TextRepoFile(UUID.randomUUID(), (short) 43);
    var postDoc2FieldsRequest = request()
        .withMethod("POST")
        .withPath(mockFieldsEndpoint)
        .withBody(getResourceAsString("fields/file.xml"));
    mockDoc2FieldsResponse(postDoc2FieldsRequest);
    var putFileRequest = request()
        .withMethod("PUT")
        .withPath(format("/%s/_doc/%s", config.elasticsearch.index, file.getId()))
        .withBody(jsonSchema(getResourceAsString("fields/fields.schema.json")));
    mockIndexFieldsResponse(putFileRequest);

    indexer.index(file, getResourceAsString("fields/file.xml"));

    mockServer.verify(postDoc2FieldsRequest, once());
    mockIndexServer.verify(putFileRequest, once());
  }

  @Test
  public void testInstantiatingElasticCustomFacetIndexer_requestsFieldUsingMultipart_whenTypeIsMultipart()
      throws IOException, IndexerException {
    var expectedContentTypeHeader = "multipart/form-data;boundary=.*";
    var config = createCustomFacetIndexerConfiguration(MULTIPART.getName());
    var fileId = UUID.randomUUID();
    mockPuttingFileResponse(config, fileId);
    mockCreatingIndexResponse(config);
    mockTypesEndpoint();
    mockMappingEndpoint();
    var indexer = new MappedIndexer(config, typeServiceMock);
    var postDocToFieldsRequest = request()
        .withMethod("POST")
        .withPath(mockFieldsEndpoint)
        .withHeader("Content-Type", expectedContentTypeHeader);
    mockDoc2FieldsResponse(postDocToFieldsRequest);

    indexer.index(new TextRepoFile(fileId, (short) 43), getResourceAsString("fields/file.xml"));

    mockServer.verify(postDocToFieldsRequest, once());
  }

  private void mockPuttingFileResponse(MappedIndexerConfiguration config, UUID fileId) throws IOException {
    var putFileRequest = request()
        .withMethod("PUT")
        .withPath(format("/%s/_doc/%s", config.elasticsearch.index, fileId))
        .withBody(jsonSchema(getResourceAsString("fields/fields.schema.json")));
    mockIndexFieldsResponse(putFileRequest);
  }

  private void mockDoc2FieldsResponse(HttpRequest request) throws IOException {
    mockServer.when(request,
        Times.exactly(1)
    ).respond(response()
        .withStatusCode(200)
        .withBody(getResourceAsString("fields/fields.json"))
    );
  }

  private void mockIndexFieldsResponse(HttpRequest request) throws IOException {
    mockIndexServer.when(request,
        Times.exactly(1)
    ).respond(response()
        .withStatusCode(200)
        .withHeader("Content-Type", "application/json")
        .withBody(getResourceAsString("fields/fields-es-response.json"))
    );
  }

  private void mockMappingEndpoint() throws IOException {
    var request = request()
        .withMethod("GET")
        .withPath(mockMappingEndpoint);
    mockGetEndpoint(getResourceAsString("indexer/test.mapping.json"), request);
  }

  private void mockTypesEndpoint() throws IOException {
    var request = request()
        .withMethod("GET")
        .withPath(mockTypesEndpoint);
    mockGetEndpoint(getResourceAsString("indexer/test.types.json"), request);
  }

  private void mockGetEndpoint(String responseBody, HttpRequest getRequest) {
    mockServer.when(getRequest,
        Times.exactly(1)
    ).respond(response()
        .withStatusCode(200)
        .withBody(responseBody)
    );
  }

  private void mockCreatingIndexResponse(MappedIndexerConfiguration config) throws IOException {
    var putIndexRequest = request()
        .withMethod("PUT")
        .withPath("/" + config.elasticsearch.index)
        // because es client changes order of fields, verify using json schema:
        .withBody(jsonSchema(getResourceAsString("indexer/test.schema.json")));
    mockCreatingIndexResponse(config.elasticsearch.index, putIndexRequest);
  }

  private void mockCreatingIndexResponse(String indexName, HttpRequest createIndexRequest) {
    mockIndexServer.when(createIndexRequest,
        Times.exactly(1)
    ).respond(response()
        .withStatusCode(200)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"acknowledged\":true,\"shards_acknowledged\":true,\"index\":\"" + indexName + "\"}")
    );

  }

  private MappedIndexerConfiguration createCustomFacetIndexerConfiguration(String type) {
    var mockEsUrl = "localhost:" + mockIndexPort;
    var config = new MappedIndexerConfiguration();
    config.name = "test-indexer";
    config.elasticsearch = new ElasticsearchConfiguration();
    config.elasticsearch.contentsField = "does-not-matter";
    config.elasticsearch.hosts = newArrayList(mockEsUrl);
    config.elasticsearch.index = "test-index";
    config.mapping = "http://localhost:" + mockPort + mockMappingEndpoint;
    config.types = "http://localhost:" + mockPort + mockTypesEndpoint;
    config.fields = FieldsConfiguration.build(type, "http://localhost:" + mockPort + mockFieldsEndpoint);
    return config;
  }


}
