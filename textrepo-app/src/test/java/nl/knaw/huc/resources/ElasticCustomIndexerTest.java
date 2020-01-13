package nl.knaw.huc.resources;

import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Type;
import nl.knaw.huc.db.TypeDao;
import nl.knaw.huc.service.index.CustomIndexerConfiguration;
import nl.knaw.huc.service.index.CustomIndexerException;
import nl.knaw.huc.service.index.ElasticCustomIndexer;
import nl.knaw.huc.service.index.ElasticsearchConfiguration;
import nl.knaw.huc.service.index.FieldsConfiguration;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpRequest;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static nl.knaw.huc.resources.TestUtils.getResourceAsString;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonSchemaBody.jsonSchema;
import static org.mockserver.verify.VerificationTimes.once;

public class ElasticCustomIndexerTest {

  private static ClientAndServer mockServer;
  private static final int mockPort = 1080;
  private static final String mockMappingEndpoint = "/mock-mapping";
  private static final String mockFieldsEndpoint = "/mock-fields";

  private static ClientAndServer mockIndexServer;
  private static final int mockIndexPort = 1081;
  private static final Jdbi jdbi = mock(Jdbi.class);
  private static final TypeDao typeDao = mock(TypeDao.class);
  private Type testType = new Type("test-type", "test/mimetype");

  @BeforeClass
  public static void setUpClass() {
    mockServer = ClientAndServer.startClientAndServer(mockPort);
    mockIndexServer = ClientAndServer.startClientAndServer(mockIndexPort);
  }

  @Before
  public void before() {
    mockServer.reset();
    mockIndexServer.reset();
    MockitoAnnotations.initMocks(this);
    when(jdbi.onDemand(TypeDao.class)).thenReturn(typeDao);
    when(typeDao.get(any())).thenReturn(Optional.of(testType));
  }

  @After
  public void resetMocks() {
    reset(jdbi, typeDao);
  }

  @AfterClass
  public static void tearDown() {
    mockServer.stop();
    mockIndexServer.stop();
  }

  @Test
  public void testInstantiationElasticCustomFacetIndexer_requestsMapping() throws IOException, CustomIndexerException {
    var config = createCustomFacetIndexerConfiguration("urlencoded", testType.getMimetype());
    var getMappingRequest = request()
        .withMethod("GET")
        .withPath(mockMappingEndpoint);
    mockMappingResponse(getResourceAsString("mapping/test.json"), getMappingRequest);
    var putIndexRequest = request()
        .withMethod("PUT")
        .withPath("/" + config.elasticsearch.index)
        // because es client changes order of fields, verify using json schema:
        .withBody(jsonSchema(getResourceAsString("mapping/test.schema.json")));
    mockCreatingIndexResponse(config.elasticsearch.index, putIndexRequest);

    new ElasticCustomIndexer(jdbi, config);

    mockServer.verify(getMappingRequest, once());
    mockIndexServer.verify(putIndexRequest, once());
  }

  @Test
  public void testIndexFile_requestsFields() throws IOException, CustomIndexerException {
    var config = createCustomFacetIndexerConfiguration("urlencoded", testType.getMimetype());
    mockMappingResponse();
    mockCreatingIndexResponse(config);
    var indexer = new ElasticCustomIndexer(jdbi, config);
    var file = new TextrepoFile(UUID.randomUUID(), (short) 43);
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

    indexer.indexFile(file, getResourceAsString("fields/file.xml"));

    mockServer.verify(postDoc2FieldsRequest, once());
    mockIndexServer.verify(putFileRequest, once());
  }

  @Test
  public void testInstantiatingElasticCustomFacetIndexer_requestsFieldUsingMultipart_whenTypeIsMultipart() throws IOException, CustomIndexerException {
    var expectedContentTypeHeader = "multipart/form-data;boundary=.*";
    var config = createCustomFacetIndexerConfiguration("multipart", testType.getMimetype());
    var fileId = UUID.randomUUID();
    mockPuttingFileResponse(config, fileId);
    mockCreatingIndexResponse(config);
    mockMappingResponse();
    var indexer = new ElasticCustomIndexer(jdbi, config);
    var postDocToFieldsRequest = request()
        .withMethod("POST")
        .withPath(mockFieldsEndpoint)
        .withHeader("Content-Type", expectedContentTypeHeader);
    mockDoc2FieldsResponse(postDocToFieldsRequest);

    indexer.indexFile(new TextrepoFile(fileId, (short) 43), getResourceAsString("fields/file.xml"));

    mockServer.verify(postDocToFieldsRequest, once());
  }

  private void mockPuttingFileResponse(CustomIndexerConfiguration config, UUID fileId) throws IOException {
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

  private void mockMappingResponse() throws IOException {
    var getMappingRequest = request()
        .withMethod("GET")
        .withPath(mockMappingEndpoint);
    mockMappingResponse(getResourceAsString("mapping/test.json"), getMappingRequest);
  }

  private void mockMappingResponse(String testMapping, HttpRequest getMappingRequest) {
    mockServer.when(getMappingRequest,
        Times.exactly(1)
    ).respond(response()
        .withStatusCode(200)
        .withBody(testMapping)
    );
  }

  private void mockCreatingIndexResponse(CustomIndexerConfiguration config) throws IOException {
    var putIndexRequest = request()
        .withMethod("PUT")
        .withPath("/" + config.elasticsearch.index)
        // because es client changes order of fields, verify using json schema:
        .withBody(jsonSchema(getResourceAsString("mapping/test.schema.json")));
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

  private CustomIndexerConfiguration createCustomFacetIndexerConfiguration(String type, String mimetype) {
    var mockMappingUrl = "http://localhost:" + mockPort + mockMappingEndpoint;
    var mockFieldsUrl = "http://localhost:" + mockPort + mockFieldsEndpoint;
    var mockEsUrl = "localhost:" + mockIndexPort;
    var config = new CustomIndexerConfiguration();
    config.elasticsearch = new ElasticsearchConfiguration();
    config.elasticsearch.contentField = "does-not-matter";
    config.elasticsearch.hosts = newArrayList(mockEsUrl);
    config.elasticsearch.index = "test-index";
    config.fields = FieldsConfiguration.build(type, mockFieldsUrl);
    config.mapping = mockMappingUrl;
    config.mimetypes = newArrayList(mimetype);
    return config;
  }
}
