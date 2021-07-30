package nl.knaw.huc.service.index;

import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Type;
import nl.knaw.huc.service.type.TypeService;
import nl.knaw.huc.service.index.config.IndexerWithMappingConfiguration;
import nl.knaw.huc.service.index.config.ElasticsearchConfiguration;
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
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonSchemaBody.jsonSchema;
import static org.mockserver.model.RegexBody.regex;
import static org.mockserver.verify.VerificationTimes.once;

public class IndexerWithMappingTest {

  private static ClientAndServer mockServer;
  private static final int mockPort = 1080;
  private static final String mockMappingEndpoint = "/mock-mapping";
  private static final String mockFieldsEndpoint = "/mock-fields";
  private static final String mockTypesEndpoint = "/mock-types";

  private static ClientAndServer mockIndexServer;
  private static final int mockIndexPort = 1081;

  // Matches mimetype configured in test.types.json:
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
  public void instantiatingIndexer_requestsMappingAndTypes() throws IOException, IndexerException {
    var config = createConfig(ORIGINAL.getName());
    var getTypesRequest = mockTypesEndpoint();
    var getMappingRequest = mockMappingEndpoint();
    var putIndexRequest = request()
        .withMethod("PUT")
        .withPath("/" + config.elasticsearch.index)
        // because es client changes order of fields, verify using json schema:
        .withBody(jsonSchema(getResourceAsString("indexer/test.schema.json")));
    mockCreatingIndexResponse(config.elasticsearch.index, putIndexRequest);

    new IndexerWithMapping(config, typeServiceMock, new TextRepoElasticClient(config.elasticsearch));

    mockServer.verify(getTypesRequest, once());
    mockServer.verify(getMappingRequest, once());
    mockIndexServer.verify(putIndexRequest, once());
  }

  @Test
  public void index_requestsFields() throws IOException, IndexerException {
    var config = createConfig(ORIGINAL.getName());
    mockTypesEndpoint();
    mockMappingEndpoint();
    mockCreatingIndexResponse(config);
    var indexer = new IndexerWithMapping(config, typeServiceMock, new TextRepoElasticClient(config.elasticsearch));
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
  public void index_requestsFields_whenIndexerDefinedNoTypes() throws IOException, IndexerException {
    var config = createConfig(ORIGINAL.getName());

    // Types endpoint without types:
    var request = request()
        .withMethod("GET")
        .withPath(mockTypesEndpoint);
    mockServer.when(request,
        Times.exactly(1)
    ).respond(response()
        .withStatusCode(204)
        .withBody("does-not-matter")
    );

    mockMappingEndpoint();
    mockCreatingIndexResponse(config);
    var indexer = new IndexerWithMapping(config, typeServiceMock, new TextRepoElasticClient(config.elasticsearch));
    var file = new TextRepoFile(UUID.randomUUID(), (short) 43);
    var postDoc2FieldsRequest = request()
        .withMethod("POST")
        .withPath(mockFieldsEndpoint)
        .withBody(getResourceAsString("fields/file.xml"));
    mockDoc2FieldsResponse(postDoc2FieldsRequest);

    var putFileRequest = request()
        .withMethod("PUT")
        .withPath(format("/%s/_doc/%s", config.elasticsearch.index, file.getId()));
    mockIndexFieldsResponse(putFileRequest);

    indexer.index(file, getResourceAsString("fields/file.xml"));

    mockServer.verify(postDoc2FieldsRequest, once());
    mockIndexServer.verify(putFileRequest, once());
  }

  @Test
  public void instantiatingElasticCustomFacetIndexer_requestsFieldUsingMultipart_whenTypeIsMultipart()
      throws IOException, IndexerException {
    var expectedContentTypeHeader = "multipart/form-data;boundary=.*";
    var config = createConfig(MULTIPART.getName());
    var fileId = UUID.randomUUID();
    mockPuttingFileResponse(config, fileId);
    mockCreatingIndexResponse(config);
    mockTypesEndpoint();
    mockMappingEndpoint();
    var indexer = new IndexerWithMapping(config, typeServiceMock, new TextRepoElasticClient(config.elasticsearch));
    var postDocToFieldsRequest = request()
        .withMethod("POST")
        .withPath(mockFieldsEndpoint)
        .withHeader("Content-Type", expectedContentTypeHeader);
    mockDoc2FieldsResponse(postDocToFieldsRequest);

    indexer.index(new TextRepoFile(fileId, (short) 43), getResourceAsString("fields/file.xml"));

    mockServer.verify(postDocToFieldsRequest, once());
  }

  @Test
  public void index_usesOriginal_whenFieldsRequestTypeIsOriginal() throws IndexerException, IOException {
    var config = createConfig(ORIGINAL.getName());
    var testFileId = UUID.randomUUID();
    var testFile = new TextRepoFile(testFileId, (short) 1);
    var latestVersionContents = "latest version contents";
    var typeService = mock(TypeService.class);
    when(typeService.getType(anyShort())).thenReturn(new Type("txt", testType.getMimetype()));
    mockTypesEndpoint();
    mockMappingEndpoint();
    var indexer = new IndexerWithMapping(config, typeServiceMock, new TextRepoElasticClient(config.elasticsearch));

    mockServer.when(
        request()
            .withMethod("PUT")
            .withPath(mockFieldsEndpoint)
    ).respond(
        response()
            .withStatusCode(200)
            .withBody("{\"acknowledged\":true,\"shards_acknowledged\":true,\"index\":\"test-index-name\"}")
            .withHeader("content-type", "application/json")
    );


    indexer.index(testFile, latestVersionContents);

    var postFieldsRequest = request()
        .withMethod("POST")
        .withPath("/mock-fields")
        .withBody(latestVersionContents)
        .withHeader("Content-Type", testType.getMimetype())
        .withHeader("Link", "</rest/files/" + testFileId + ">; rel=\"original\"");
    mockServer.verify(postFieldsRequest);
  }

  @Test
  public void index_usesMultipartFormData_whenFieldsTypeIsMultipart() throws IndexerException, IOException {
    var fieldsType = MULTIPART.getName();
    var config = createConfig(fieldsType);
    var testFileId = UUID.randomUUID();
    var testFile = new TextRepoFile(testFileId, (short) 1);
    var latestVersionContents = "latest version contents";
    var typeService = mock(TypeService.class);
    when(typeService.getType(anyShort())).thenReturn(new Type("txt", testType.getMimetype()));
    mockTypesEndpoint();
    mockMappingEndpoint();
    var indexer = new IndexerWithMapping(config, typeServiceMock, new TextRepoElasticClient(config.elasticsearch));

    indexer.index(testFile, latestVersionContents);

    var postFieldsRequest = request()
        .withMethod("POST")
        .withPath(mockFieldsEndpoint)
        .withHeader("Content-Type", "multipart/form-data.*")
        .withBody(regex("(.|\n|\r)*" +
            "Link: </rest/files/" + testFileId + ">; rel=\"original\"(.|\n|\r)*" +
            "Content-Type: " + testType.getMimetype() + "(.|\n|\r)*" +
            "name=\"file\"(.|\n|\r)*" +
            "latest version contents(.|\n|\r)*"));
    mockServer.verify(postFieldsRequest);
  }

  private void mockPuttingFileResponse(IndexerWithMappingConfiguration config, UUID fileId) throws IOException {
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

  private HttpRequest mockMappingEndpoint() throws IOException {
    var request = request()
        .withMethod("GET")
        .withPath(mockMappingEndpoint);
    mockGetEndpoint(getResourceAsString("indexer/test.mapping.json"), request);
    return request;
  }

  private HttpRequest mockTypesEndpoint() throws IOException {
    var request = request()
        .withMethod("GET")
        .withPath(mockTypesEndpoint);
    mockGetEndpoint(getResourceAsString("indexer/test.types.json"), request);
    return request;
  }

  private void mockGetEndpoint(String responseBody, HttpRequest getRequest) {
    mockServer.when(getRequest,
        Times.exactly(1)
    ).respond(response()
        .withStatusCode(200)
        .withBody(responseBody)
    );
  }

  private void mockCreatingIndexResponse(IndexerWithMappingConfiguration config) throws IOException {
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

  private IndexerWithMappingConfiguration createConfig(String type) {
    var mockEsUrl = "localhost:" + mockIndexPort;
    var config = new IndexerWithMappingConfiguration();
    config.name = "test-indexer";
    config.elasticsearch = new ElasticsearchConfiguration();
    config.elasticsearch.index = "test-index";
    config.elasticsearch.contentsField = "does-not-matter";
    config.elasticsearch.hosts = newArrayList(mockEsUrl);
    config.mapping = "http://localhost:" + mockPort + mockMappingEndpoint;
    config.types = "http://localhost:" + mockPort + mockTypesEndpoint;
    config.fields = FieldsConfiguration.build(type, "http://localhost:" + mockPort + mockFieldsEndpoint);
    return config;
  }


}
