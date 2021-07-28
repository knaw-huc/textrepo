package nl.knaw.huc.service.index;

import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.service.index.request.IndexerFieldsRequestFactory;
import nl.knaw.huc.service.type.TypeService;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static java.lang.String.join;
import static nl.knaw.huc.service.index.FieldsType.MULTIPART;
import static org.elasticsearch.common.xcontent.XContentType.JSON;

/**
 * MappedFileIndexer creates its index in elasticsearch as defined by its es-mapping
 * which can be retrieved at its `mapping` endpoint
 *
 * <p>MappedFileIndexer adds new files to its index in two steps:
 * 1. convert file contents to an es-doc at its `fields` endpoint
 * 2. sends index-request with es-doc to its index
 *
 * <p>MappedFileIndexer depends on a REST-service with the following two endpoints:
 * - GET `mapping`
 * - POST `fields`
 *
 * <p>MappedFileIndexer is configured in config.yml
 */
public class MappedIndexer implements Indexer {

  private static final Logger log = LoggerFactory.getLogger(MappedIndexer.class);
  private final MappedIndexerConfiguration config;
  private final Client requestClient = JerseyClientBuilder.newClient();
  private final TypeService typeService;
  private final TextRepoElasticClient client;
  private final IndexerFieldsRequestFactory fieldsRequestFactory;

  public MappedIndexer(
      MappedIndexerConfiguration config,
      TypeService typeService,
      TextRepoElasticClient textRepoElasticClient
  ) throws IndexerException {
    this.config = config;
    this.typeService = typeService;
    this.client = textRepoElasticClient;
    this.fieldsRequestFactory = new IndexerFieldsRequestFactory(config.fields.url, this.requestClient);

    createIndex(config);
    if (MULTIPART.equals(config.fields.type)) {
      requestClient.register(MultiPartFeature.class);
    }
  }

  private void createIndex(MappedIndexerConfiguration config) throws IndexerException {
    log.info("Creating es index [{}]", config.elasticsearch.index);
    var response = getMapping(config);
    var mappingResult = response.readEntity(String.class);

    if (response.getStatus() != 200) {
      log.error(
          "Could not get mapping from [{}]: {} - {}",
          config.mapping, response.getStatus(), mappingResult
      );
      return;
    }

    var request = new CreateIndexRequest(config.elasticsearch.index)
        .source(mappingResult, JSON);

    try {
      client
          .getClient()
          .indices()
          .create(request, RequestOptions.DEFAULT);
    } catch (ElasticsearchStatusException ex) {
      log.info("Could not create index [{}], already exists", config.elasticsearch.index);
    } catch (IOException ex) {
      log.error("Could not create index [{}]", config.elasticsearch.index, ex);
    }
  }

  @Override
  public Optional<String> index(@Nonnull TextRepoFile file, @Nonnull String latestVersionContents) {
    var mimetype = typeService.getType(file.getTypeId()).getMimetype();
    if (!mimetypeSupported(mimetype)) {
      return Optional.empty();
    }

    var response = getFields(latestVersionContents, mimetype, file.getId());
    var esFacets = response.readEntity(String.class);

    var error = checkIndexerResponseStatus(response, esFacets);
    if (error.isPresent()) {
      return error;
    }

    return sendRequest(file.getId(), esFacets);
  }

  @Override
  public IndexerConfiguration getConfig() {
    return this.config;
  }

  private Response getMapping(MappedIndexerConfiguration config) throws IndexerException {
    Response response;
    try {
      response = requestClient
          .target(config.mapping)
          .request()
          .get();
      return response;
    } catch (ProcessingException ex) {
      throw new IndexerException(format("Could not fetch mapping from %s", config.mapping), ex);
    }
  }

  private Response getFields(@Nonnull String latestVersionContents, String mimetype, UUID fileId) {
    return fieldsRequestFactory
        .build(config.fields.type)
        .requestFields(latestVersionContents, mimetype, fileId);
  }

  private Optional<String> sendRequest(@Nonnull UUID fileId, String esFacets) {
    var indexRequest = new IndexRequest(config.elasticsearch.index)
        .id(fileId.toString())
        .source(esFacets, JSON);
    var response = indexRequest(indexRequest);
    return checkEsResponseStatus(response, fileId);
  }

  private IndexResponse indexRequest(IndexRequest indexRequest) {
    try {
      return client.getClient().index(indexRequest, RequestOptions.DEFAULT);
    } catch (Exception ex) {
      throw new WebApplicationException("Could not index in Elasticsearch", ex);
    }
  }

  private boolean mimetypeSupported(String mimetype) {
    if (!config.mimetypes.contains(mimetype)) {
      log.info("Not indexing in {} because {} is not in [{}]",
          config.elasticsearch.index, mimetype, join(", ", config.mimetypes)
      );
      return false;
    }
    return true;
  }

  /**
   * When not 200 or 201, return error msg
   */
  private Optional<String> checkEsResponseStatus(IndexResponse response, UUID fileId) {
    var status = response.status().getStatus();
    var index = config.elasticsearch.index;

    if (status == 201) {
      log.debug("Successfully added file [{}] to index [{}]", fileId, index);
      return Optional.empty();
    } else if (status == 200) {
      log.debug("Successfully updated file [{}] to index [{}]", fileId, index);
      return Optional.empty();
    } else {
      var msg = format(
          "Response of adding file %s to index %s was: %d - %s",
          fileId, index, status, response.toString()
      );
      log.error(msg);
      return Optional.of(msg);
    }
  }

  /**
   * When not 200, return error msg
   */
  private Optional<String> checkIndexerResponseStatus(Response response, String esBody) {
    if (response.getStatus() != 200) {
      final var msg = format(
          "Could not get fields for %s, response was: %d - %s",
          config.elasticsearch.index, response.getStatus(), esBody
      );
      log.error(msg);
      return Optional.of(msg);
    }
    return Optional.empty();
  }

}
