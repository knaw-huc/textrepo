package nl.knaw.huc.service.index;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Type;
import nl.knaw.huc.service.type.TypeService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockserver.integration.ClientAndServer;

import java.util.UUID;

import static java.util.Arrays.asList;
import static nl.knaw.huc.service.index.FieldsType.MULTIPART;
import static nl.knaw.huc.service.index.FieldsType.ORIGINAL;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.RegexBody.regex;

@ExtendWith(DropwizardExtensionsSupport.class)
public class MappedIndexerTest {

  private static ClientAndServer mockServer;
  private static final int mockPort = 1080;

  @BeforeAll
  public static void setUpClass() {
    mockServer = ClientAndServer.startClientAndServer(mockPort);
  }

  @BeforeEach
  public void before() {
    mockServer.reset();
    MockitoAnnotations.initMocks(this);
  }

  @AfterAll
  public static void tearDown() {
    mockServer.stop();
  }

  @Test
  public void index_usesOriginal_whenFieldsTypeIsOriginal() throws IndexerException {
    var fieldsType = ORIGINAL.getName();
    var config = createConfig(fieldsType);
    var testFileId = UUID.randomUUID();
    var testFile = new TextRepoFile(testFileId, (short) 1);
    var latestVersionContents = "latest version contents";
    var typeService = mock(TypeService.class);
    when(typeService.getType(anyShort())).thenReturn(new Type("txt", "text/plain"));
    var mappedIndexer = initializeIndexer(config, typeService);

    mappedIndexer.index(testFile, latestVersionContents);

    var postFieldsRequest = request()
        .withMethod("POST")
        .withPath("/fields")
        .withBody(latestVersionContents)
        .withHeader("Content-Type", "text/plain")
        .withHeader("Link", "</rest/files/" + testFileId + ">; rel=\"original\"");
    mockServer.verify(postFieldsRequest);
  }

  @Test
  public void index_usesMultipartFormData_whenFieldsTypeIsMultipart() throws IndexerException {
    var fieldsType = MULTIPART.getName();
    var config = createConfig(fieldsType);
    var testFileId = UUID.randomUUID();
    var testFile = new TextRepoFile(testFileId, (short) 1);
    var latestVersionContents = "latest version contents";
    var mimetype = "text/plain";
    var typeService = mock(TypeService.class);
    when(typeService.getType(anyShort())).thenReturn(new Type("txt", mimetype));
    var mappedIndexer = initializeIndexer(config, typeService);

    mappedIndexer.index(testFile, latestVersionContents);

    var postFieldsRequest = request()
        .withMethod("POST")
        .withPath("/fields")
        .withHeader("Content-Type", "multipart/form-data.*")
        .withBody(regex("(.|\n|\r)*" +
            "Link: </rest/files/" + testFileId + ">; rel=\"original\"(.|\n|\r)*" +
            "Content-Type: text/plain(.|\n|\r)*" +
            "name=\"file\"(.|\n|\r)*" +
            "latest version contents(.|\n|\r)*"));
    mockServer.verify(postFieldsRequest);
  }

  private MappedIndexer initializeIndexer(
      MappedIndexerConfiguration config,
      TypeService typeService
  ) throws IndexerException {
    mockServer.when(
        request()
            .withMethod("GET")
            .withPath("/mapping")
    ).respond(
        response()
            .withStatusCode(200)
            .withBody("{\"mappings\": {\"properties\": {}}}")
    );
    mockServer.when(
        request()
            .withMethod("PUT")
            .withPath("/test-index-name")
    ).respond(
        response()
            .withStatusCode(200)
            .withBody("{\"acknowledged\":true,\"shards_acknowledged\":true,\"index\":\"test-index-name\"}")
            .withHeader("content-type", "application/json")
    );
    return new MappedIndexer(config, typeService, new TextRepoElasticClient(config.elasticsearch));
  }

  private MappedIndexerConfiguration createConfig(String fieldsType) {
    var host = "http://localhost:" + mockPort;
    var config = new MappedIndexerConfiguration();
    config.elasticsearch = new ElasticsearchConfiguration();
    config.elasticsearch.hosts = asList(host.replace("http://", ""));
    config.elasticsearch.index = "test-index-name";
    config.mapping = host + "/mapping";
    config.fields = new FieldsConfiguration();
    config.fields.type = FieldsType.fromString(fieldsType);
    config.fields.url = host + "/fields";
    config.mimetypes = asList("text/plain");
    return config;
  }


}
