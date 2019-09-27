package nl.knaw.huc.resources;

import nl.knaw.huc.service.index.CustomFacetIndexerConfiguration;
import nl.knaw.huc.service.index.ElasticCustomFacetIndexer;
import nl.knaw.huc.service.index.ElasticsearchConfiguration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpRequest;

import java.io.IOException;
import java.util.UUID;

import static java.lang.String.format;
import static nl.knaw.huc.resources.TestUtils.getResourceAsString;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonSchemaBody.jsonSchema;
import static org.mockserver.verify.VerificationTimes.once;

public class ElasticCustomFacetIndexerTest {

  private static ClientAndServer mockServer;
  private static final int mockPort = 1080;
  private static final String mockMappingEndpoint = "/mock-mapping";
  private static final String mockFieldsEndpoint = "/mock-fields";

  private static ClientAndServer mockIndexServer;
  private static final int mockIndexPort = 1081;

  @BeforeClass
  public static void setUpClass() {
    mockServer = ClientAndServer.startClientAndServer(mockPort);
    mockIndexServer = ClientAndServer.startClientAndServer(mockIndexPort);
  }

  @Before
  public void reset() {
    mockServer.reset();
    mockIndexServer.reset();
  }

  @AfterClass
  public static void tearDown() {
    mockServer.stop();
    mockIndexServer.stop();
  }

  @Test
  public void testInstantiationElasticCustomFacetIndexer_requestsMapping() throws IOException {
    var config = createCustomFacetIndexerConfiguration();
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

    new ElasticCustomFacetIndexer(config);

    mockServer.verify(getMappingRequest, once());
    mockIndexServer.verify(putIndexRequest, once());
  }

  @Test
  public void testIndexDocument_requestsFields() throws IOException {
    var config = createCustomFacetIndexerConfiguration();
    mockMappingResponse();
    mockCreatingIndexResponse(config);
    var indexer = new ElasticCustomFacetIndexer(config);
    var documentId = UUID.randomUUID();
    var postDoc2FieldsRequest = request()
        .withMethod("POST")
        .withPath(mockFieldsEndpoint)
        .withBody(getResourceAsString("fields/document.xml"));
    mockDoc2FieldsResponse(postDoc2FieldsRequest);
    var putDocumentRequest = request()
        .withMethod("PUT")
        .withPath(format("/%s/_doc/%s", config.elasticsearch.index, documentId))
        .withBody(jsonSchema(getResourceAsString("fields/fields.schema.json")));
    mockIndexFieldsResponse(putDocumentRequest);

    indexer.indexDocument(documentId, getResourceAsString("fields/document.xml"));

    mockServer.verify(postDoc2FieldsRequest, once());
    mockIndexServer.verify(putDocumentRequest, once());
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

  private void mockCreatingIndexResponse(CustomFacetIndexerConfiguration config) throws IOException {
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

  private CustomFacetIndexerConfiguration createCustomFacetIndexerConfiguration() {
    var mockMappingUrl = "http://localhost:" + mockPort + mockMappingEndpoint;
    var mockFieldsUrl = "http://localhost:" + mockPort + mockFieldsEndpoint;
    var mockEsUrl = "localhost:" + mockIndexPort;
    var config = new CustomFacetIndexerConfiguration();
    config.elasticsearch = new ElasticsearchConfiguration();
    config.elasticsearch.contentField = "does-not-matter";
    config.elasticsearch.hosts = newArrayList(mockEsUrl);
    config.elasticsearch.index = "test-index";
    config.fields = mockFieldsUrl;
    config.mapping = mockMappingUrl;
    return config;
  }
}
