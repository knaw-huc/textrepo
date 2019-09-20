package nl.knaw.huc.service.index;

import io.dropwizard.lifecycle.Managed;
import nl.knaw.huc.CustomFacetIndexerConfiguration;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;
import java.io.IOException;
import java.util.UUID;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.elasticsearch.common.xcontent.XContentType.JSON;


public class ElasticCustomFacetIndexer implements DocumentIndexer, Managed {

  private final String fieldsEndpoint;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private ElasticDocumentIndexer indexer;

  private Client jerseyClient = JerseyClientBuilder.newClient();

  public ElasticCustomFacetIndexer(CustomFacetIndexerConfiguration config) {
    indexer = new ElasticDocumentIndexer(config.elasticsearch);
    createIndex(config);
    this.fieldsEndpoint = config.fields;
  }

  private void createIndex(CustomFacetIndexerConfiguration config) {
    var esMapping = jerseyClient
        .target(config.mapping)
        .request()
        .get().readEntity(String.class);

    var client = new TextRepoElasticClient(config.elasticsearch);
    var request = new CreateIndexRequest(config.elasticsearch.index)
        .source(esMapping, JSON);
    try {
      client
          .getClient()
          .indices()
          .create(request, RequestOptions.DEFAULT);
    } catch (IOException ex) {
      logger.error("Could not create index [{}]", config.elasticsearch.index, ex);
    }
  }

  @Override
  public void indexDocument(@Nonnull UUID document, @NotNull String latestVersionContent) {
    var esFacets = jerseyClient
        .target(fieldsEndpoint)
        .request()
        .post(entity(latestVersionContent, APPLICATION_JSON_TYPE))
        .readEntity(String.class);

    indexer.indexDocument(document, esFacets);
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() throws Exception {
    indexer.stop();
  }

}
