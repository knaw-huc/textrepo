package nl.knaw.huc.service.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huc.api.FormIndexerType;
import nl.knaw.huc.service.index.config.IndexerConfiguration;
import nl.knaw.huc.service.index.config.IndexerWithMappingConfiguration;
import nl.knaw.huc.service.index.request.IndexerFieldsRequestFactory;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static nl.knaw.huc.service.index.FieldsType.MULTIPART;

/**
 * Indexer creates ES Docs as defined by its es-mapping endpoint
 *
 * <p>Indexer depends on a REST-service with the following endpoints:
 * - GET `mapping`
 * - GET `types`
 * - POST `fields`
 *
 * <p>Indexer is configured in config.yml
 *
 * <p>Types and mapping are requesting during initialization phase
 */
public class IndexerWithMappingClient implements IndexerClient {

  private static final Logger log = LoggerFactory.getLogger(IndexerWithMappingClient.class);
  private final IndexerWithMappingConfiguration config;
  private final Client requestClient = JerseyClientBuilder.newClient();
  private final IndexerFieldsRequestFactory fieldsRequestFactory;
  private final Optional<List<String>> mimetypes;

  public IndexerWithMappingClient(
      IndexerWithMappingConfiguration config
  ) {
    this.config = config;
    this.mimetypes = getIndexerTypes();
    this.fieldsRequestFactory = new IndexerFieldsRequestFactory(config.fields.url, this.requestClient);

    if (MULTIPART.equals(config.fields.type)) {
      requestClient.register(MultiPartFeature.class);
    }
  }

  @Override
  public Optional<String> fields(@Nonnull UUID file, @Nonnull String mimetype, @Nonnull String contents) {
    var indexName = config.elasticsearch.index;
    if (!mimetypeSupported(mimetype)) {
      log.info(format(
          "Skipping file %s for index %s: mimetype not supported",
          file, indexName
      ));
      return Optional.empty();
    }

    log.info(format("Converting file %s to ES doc for index %s", file, indexName));

    var response = getFields(contents, mimetype, file);
    var responseBody = response.readEntity(String.class);

    var error = checkIndexerResponseStatus(response, responseBody);
    if (error.isPresent()) {
      log.error(error.get());
      return Optional.empty();
    } else {
      return Optional.of(responseBody);
    }
  }

  @Override
  public IndexerConfiguration getConfig() {
    return this.config;
  }

  public Optional<List<String>> getMimetypes() {
    return this.mimetypes;
  }

  public Optional<String> getMapping() {
    log.info("Get mapping of {} at {}", config.elasticsearch.index, config.mapping);
    Response response;
    try {
      response = requestClient
          .target(config.mapping)
          .request()
          .get();
    } catch (ProcessingException ex) {
      throw noMappingFound(config.mapping, ex.getMessage());
    }
    if (response.getStatus() != 200) {
      throw noMappingFound(config.mapping, format("Status was %s instead of 200", response.getStatus()));
    }
    return Optional.of(response.readEntity(String.class));
  }

  private WebApplicationException noMappingFound(String mappingUrl, String msg) {
    return new WebApplicationException(format("Could not fetch mapping from %s: %s", mappingUrl, msg));
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
      throw new WebApplicationException(format("Could not parse types response of indexer %s: %s", config.name, json));
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
