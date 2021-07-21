package nl.knaw.huc.service.task.indexer;

import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.db.TypesDao;
import nl.knaw.huc.service.index.ElasticsearchConfiguration;
import nl.knaw.huc.service.index.FieldsConfiguration;
import nl.knaw.huc.service.index.Indexer;
import nl.knaw.huc.service.index.IndexerException;
import nl.knaw.huc.service.index.MappedIndexer;
import nl.knaw.huc.service.index.MappedIndexerConfiguration;
import nl.knaw.huc.service.index.TextRepoElasticClient;
import nl.knaw.huc.service.type.TypeService;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockserver.integration.ClientAndServer;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.common.collect.List.of;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class JdbiIndexAllFilesByIndexTaskTest {

  private static final int mockPort = 1080;
  private static final String mockEsUrl = "http://localhost:" + mockPort;
  private static final String mapping = "/mapping";
  private static final String mockMappingUrl = mockEsUrl + mapping;
  private static final String testMimetype = "test/mimetype";

  private final static Jdbi JDBI = mock(Jdbi.class);
  private final static TypeService TYPE_SERVICE = mock(TypeService.class);
  private final static TextRepoElasticClient TR_ES_CLIENT = mock(TextRepoElasticClient.class);
  private final static RestHighLevelClient ES_CLIENT = mock(RestHighLevelClient.class);
  private final static IndicesClient ES_INDICES_CLIENT = mock(IndicesClient.class);
  private static final TypesDao TYPES_DAO = mock(TypesDao.class);
  private static final FilesDao FILES_DAO = mock(FilesDao.class);

  private static ClientAndServer mockServer;

  @BeforeAll
  public static void setUpClass() {
    mockServer = ClientAndServer.startClientAndServer(mockPort);
  }

  @BeforeEach
  public void setUp() {
    mockServer.reset();
    MockitoAnnotations.initMocks(this);
  }

  @AfterAll
  public static void tearDown() {
    mockServer.stop();
  }

  @AfterEach
  public void resetMocks() {
    reset(JDBI, TYPE_SERVICE, TR_ES_CLIENT, ES_CLIENT, TYPES_DAO, FILES_DAO);
  }

  @Test
  public void testRun_skipsIndexing_whenNoTypes() throws IndexerException, IOException {
    var indexerTask = createIndexerBuilder();

    when(JDBI.onDemand(TypesDao.class)).thenReturn(TYPES_DAO);
    when(TYPES_DAO.findByMimetype(testMimetype)).thenReturn(Optional.empty());

    var result = indexerTask.run();
    verify(TYPES_DAO, times(1)).findByMimetype(testMimetype);
    assertThat(result).contains("No types to index");
  }

  @Test
  public void testRun_countsAndIndexes_whenTypeFound() throws IndexerException {
    var indexerTask = createIndexerBuilder();

    when(JDBI.onDemand(TypesDao.class)).thenReturn(TYPES_DAO);
    when(JDBI.onDemand(FilesDao.class)).thenReturn(FILES_DAO);
    short typeId = 1;
    when(TYPES_DAO.findByMimetype(testMimetype)).thenReturn(Optional.of(typeId));
    when(FILES_DAO.countByTypes(of(typeId))).thenReturn(1L);

    var result = indexerTask.run();

    verify(TYPES_DAO, times(1)).findByMimetype(testMimetype);
    verify(FILES_DAO, times(1)).countByTypes(of(typeId));
    verify(FILES_DAO, times(1)).foreachByType(eq(typeId), any());

    // 0 (and not 1) files affected, because FILES_DAO.foreachByType() is mocked:
    assertThat(result).contains("Total files affected");
    assertThat(result).contains("0");
  }

  private JdbiIndexFileTaskBuilder.JdbiIndexAllFilesByIndexTask createIndexerBuilder() throws IndexerException {
    var config = createIndexerConfig();
    mockIndexerMappingEndpoint();
    mockIndexCreation();
    var indexer = new MappedIndexer(config, TYPE_SERVICE, TR_ES_CLIENT);
    List<Indexer> indexers = List.of(indexer);

    // Because we set index name, we know to what type to cast to:
    return (JdbiIndexFileTaskBuilder.JdbiIndexAllFilesByIndexTask) new JdbiIndexFileTaskBuilder(JDBI, indexers)
        .forIndex(config.name)
        .build();
  }

  private void mockIndexCreation() {
    when(TR_ES_CLIENT.getClient()).thenReturn(ES_CLIENT);
    when(ES_CLIENT.indices()).thenReturn(ES_INDICES_CLIENT);
  }

  private void mockIndexerMappingEndpoint() {
    mockServer.when(request()
        .withMethod("GET")
        .withPath(mapping)
    ).respond(response()
        .withStatusCode(200)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"mappings\": {\"properties\": {}}}")
    );
  }

  private MappedIndexerConfiguration createIndexerConfig() {
    var config = new MappedIndexerConfiguration();
    config.name = "test-indexer";
    config.elasticsearch = new ElasticsearchConfiguration();
    config.elasticsearch.index = "test-index";
    config.mapping = mockMappingUrl;
    config.fields = new FieldsConfiguration();
    config.fields.url = mockEsUrl;
    config.mimetypes = of(testMimetype);
    return config;
  }

}
