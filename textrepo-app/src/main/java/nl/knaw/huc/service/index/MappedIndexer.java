package nl.knaw.huc.service.index;

import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.service.TypeService;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static java.lang.String.join;
import static javax.ws.rs.client.Entity.entity;
import static org.elasticsearch.common.xcontent.XContentType.JSON;

/**
 * MappedFileIndexer creates its index in elasticsearch:
 * - as defined by its es-mapping which can be retrieved at its `mapping` endpoint
 * MappedFileIndexer adds new files to its index:
 * - convert file contents to an es-doc at its `fields` endpoint
 * - sends index-request with es-doc to its index
 * MappedFileIndexer depends on a REST-service with the following two endpoints:
 * - GET `mapping`
 * - POST `fields` (using urlencoded or multipart)
 * MappedFileIndexer is configured in config.yml
 */
public class MappedIndexer implements Indexer {

  private static final Logger log = LoggerFactory.getLogger(MappedIndexer.class);
  private final MappedIndexerConfiguration config;
  private final Client requestClient = JerseyClientBuilder.newClient();
  private final TypeService typeService;
  private final TextRepoElasticClient client;

  public MappedIndexer(
      MappedIndexerConfiguration config,
      TypeService typeService
  ) throws IndexerException {
    this.config = config;
    this.typeService = typeService;
    this.client = new TextRepoElasticClient(config.elasticsearch);

    createIndex(config);
    if (config.fields.type.equals("multipart")) {
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

    var response = getFields(latestVersionContents, mimetype);
    var esFacets = response.readEntity(String.class);

    var fieldStatusComplaint = checkFieldStatus(response, esFacets);
    if (fieldStatusComplaint.isPresent()) {
      return fieldStatusComplaint;
    }

    return sendRequest(file.getId(), esFacets);
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

  private Response getFields(@Nonnull String latestVersionContents, String mimetype) {
    switch (config.fields.type) {
      case "urlencoded":
        return getFieldsUrlencoded(latestVersionContents, mimetype);
      case "multipart":
        return getFieldsMultiparted(latestVersionContents, mimetype);
      default:
        throw new IllegalStateException(format(
            "Fields type [%s] of [%s] does not exist",
            config.elasticsearch.index,
            config.fields.type
        ));
    }
  }

  private Response getFieldsUrlencoded(@Nonnull String latestVersionContents, String mimetype) {
    return requestClient
        .target(config.fields.url)
        .request()
        .post(entity(latestVersionContents, mimetype));
  }

  private Response getFieldsMultiparted(@Nonnull String latestVersionContents, String mimetype) {
    return postMultiparted(latestVersionContents.getBytes(), mimetype);
  }

  private Response postMultiparted(byte[] bytes, String mimetype) {
    var contentDisposition = FormDataContentDisposition
        .name("file")
        .size(bytes.length)
        .build();

    var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(contentDisposition, bytes, MediaType.valueOf(mimetype)));

    var request = requestClient
        .target(config.fields.url)
        .request();

    var entity = entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }

  private Optional<String> sendRequest(@Nonnull UUID fileId, String esFacets) {
    var indexRequest = new IndexRequest(config.elasticsearch.index)
        .id(fileId.toString())
        .source(esFacets, JSON);
    var response = indexRequest(indexRequest);
    var status = response.status().getStatus();
    if (status == 201) {
      log.debug("Successfully added file [{}] to index [{}]", fileId, config.elasticsearch.index);
      return Optional.empty();
    } else {
      final var msg = format("Response of adding file %s to index %s was: %d - %s",
          fileId, config.elasticsearch.index, status, response.toString());
      log.error(msg);
      return Optional.of(msg);
    }
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

  private Optional<String> checkFieldStatus(Response response, String esFacets) {
    if (response.getStatus() != 200) {
      final var msg = format("Could not get fields for %s, response was: %d - %s",
          config.elasticsearch.index, response.getStatus(), esFacets);
      log.error(msg);
      return Optional.of(msg);
    }
    return Optional.empty();
  }

}
