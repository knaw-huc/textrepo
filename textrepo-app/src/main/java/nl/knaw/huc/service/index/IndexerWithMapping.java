package nl.knaw.huc.service.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huc.api.FormIndexerType;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.service.index.config.IndexerConfiguration;
import nl.knaw.huc.service.index.config.IndexerWithMappingConfiguration;
import nl.knaw.huc.service.index.request.IndexerFieldsRequestFactory;
import nl.knaw.huc.service.type.TypeService;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static nl.knaw.huc.service.index.FieldsType.MULTIPART;
import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.elasticsearch.common.xcontent.XContentType.JSON;

/**
 * MappedFileIndexer creates its index in elasticsearch as defined by its es-mapping
 * which can be retrieved at its `mapping` endpoint
 *
 * <p>MappedFileIndexer adds new files to its index in two steps:
 * 1. convert file contents to an es-doc at its `fields` endpoint
 * 2. sends index-request with es-doc to its index
 *
 * <p>MappedFileIndexer depends on a REST-service with the following endpoints:
 * - GET `mapping`
 * - GET `types`
 * - POST `fields`
 *
 * <p>MappedFileIndexer is configured in config.yml
 * <p>Types and mapping are requesting during initialization fase
 */
public class IndexerWithMapping implements Indexer {

  private static final Logger log = LoggerFactory.getLogger(IndexerWithMapping.class);
  private final IndexerWithMappingConfiguration config;
  private final Client requestClient = JerseyClientBuilder.newClient();
  private final TypeService typeService;
  private final TextRepoElasticClient client;
  private final IndexerFieldsRequestFactory fieldsRequestFactory;
  private final Optional<List<String>> mimetypes;

  public IndexerWithMapping(
      IndexerWithMappingConfiguration config,
      TypeService typeService,
      TextRepoElasticClient textRepoElasticClient
  ) throws IndexerException {
    this.config = config;
    this.typeService = typeService;
    this.mimetypes = getIndexerTypes();
    this.client = textRepoElasticClient;
    this.fieldsRequestFactory = new IndexerFieldsRequestFactory(config.fields.url, this.requestClient);

    createIndex(config);
    if (MULTIPART.equals(config.fields.type)) {
      requestClient.register(MultiPartFeature.class);
    }
  }

  @Override
  public Optional<String> index(@Nonnull TextRepoFile file, @Nonnull String latestVersionContents) {
    var mimetype = typeService.getType(file.getTypeId()).getMimetype();
    if (!mimetypeSupported(mimetype)) {
      return Optional.empty();
    }

    log.info(format("Adding file %s to index %s", file.getId(), config.elasticsearch.index));

    var response = getFields(latestVersionContents, mimetype, file.getId());
    var esFacets = response.readEntity(String.class);

    var error = checkIndexerResponseStatus(response, esFacets);
    if (error.isPresent()) {
      return error;
    }

    return client.upsert(file.getId(), esFacets);
  }

  @Override
  public IndexerConfiguration getConfig() {
    return this.config;
  }

  public Optional<List<String>> getMimetypes() {
    return this.mimetypes;
  }

  private void createIndex(IndexerWithMappingConfiguration config) throws IndexerException {
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
    client.createIndex(mappingResult);
  }

  private Response getMapping(IndexerWithMappingConfiguration config) throws IndexerException {
    Response response;
    try {
      response = requestClient
          .target(config.mapping)
          .request()
          .get();
      return response;
    } catch (ProcessingException ex) {
      throw new WebApplicationException(format("Could not fetch mapping from %s", config.mapping), ex);
    }
  }

  private Optional<List<String>> getIndexerTypes() {
    var response = this.requestClient
        .target(config.types)
        .request()
        .get();
    var json = response.readEntity(String.class);
    if (response.getStatus() == NO_CONTENT.getStatusCode()) {
      return Optional.empty();
    }
    try {
      var parsed = new ObjectMapper().readValue(json, new TypeReference<List<FormIndexerType>>() {
      });
      return Optional.of(parsed
          .stream()
          .map(t -> t.mimetype)
          .collect(toList()));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(format("Could not parse types response of indexer %s: %s", config.name, json));
    }
  }

  private Response getFields(@Nonnull String latestVersionContents, String mimetype, UUID fileId) {
    return fieldsRequestFactory
        .build(config.fields.type)
        .requestFields(latestVersionContents, mimetype, fileId);
  }

  private boolean mimetypeSupported(String mimetype) {
    if (mimetypes.isEmpty()) {
      return true;
    }
    if (!mimetypes.get().contains(mimetype)) {
      log.info("Not indexing in {} because {} is not in [{}]",
          config.elasticsearch.index, mimetype, join(", ", mimetypes.get())
      );
      return false;
    }
    return true;
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
