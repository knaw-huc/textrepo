package nl.knaw.huc.service.index;

import io.dropwizard.lifecycle.Managed;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.index.IndexRequest;
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
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static org.elasticsearch.common.xcontent.XContentType.JSON;


public class ElasticCustomFacetIndexer implements FileIndexer, Managed {

  private final CustomFacetIndexerConfiguration config;
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private ElasticFileIndexer indexer;

  private Client jerseyClient = JerseyClientBuilder.newClient();

  public ElasticCustomFacetIndexer(CustomFacetIndexerConfiguration config) {
    this.config = config;
    indexer = new ElasticFileIndexer(config.elasticsearch);
    createIndex(config);
  }

  private void createIndex(CustomFacetIndexerConfiguration config) {
    var response = jerseyClient
        .target(config.mapping)
        .request()
        .get();

    var mappingResult = response
        .readEntity(String.class);

    if (response.getStatus() != 200) {
      logger.error("Could not get mapping: {} - {}", response.getStatus(), mappingResult);
      return;
    }

    var client = new TextRepoElasticClient(config.elasticsearch);
    var request = new CreateIndexRequest(config.elasticsearch.index)
        .source(mappingResult, JSON);

    try {
      client
          .getClient()
          .indices()
          .create(request, RequestOptions.DEFAULT);
    } catch (ElasticsearchStatusException ex) {
      logger.warn("Could not create index [{}], already exists", config.elasticsearch.index);
    } catch (IOException ex) {
      logger.error("Could not create index [{}]", config.elasticsearch.index, ex);
    }
  }

  @Override
  public void indexFile(@Nonnull UUID fileId, @NotNull String latestVersionContent) {
    var response = jerseyClient
        .target(config.fields)
        .request()
        .post(entity(latestVersionContent, APPLICATION_XML_TYPE));
    var esFacets = response
        .readEntity(String.class);

    if (response.getStatus() != 200) {
      logger.error("Could not get fields: {} - {}", response.getStatus(), esFacets);
      return;
    }

    var indexRequest = new IndexRequest(this.config.elasticsearch.index)
        .id(fileId.toString())
        .source(esFacets, JSON);

    indexer.indexRequest(indexRequest);
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() throws Exception {
    indexer.stop();
  }

}
