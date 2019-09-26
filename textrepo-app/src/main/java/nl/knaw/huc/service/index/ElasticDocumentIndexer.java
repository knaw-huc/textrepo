package nl.knaw.huc.service.index;

import io.dropwizard.lifecycle.Managed;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.UUID;

public class ElasticDocumentIndexer implements DocumentIndexer, Managed {

  private TextRepoElasticClient client;
  private final ElasticsearchConfiguration config;

  public ElasticDocumentIndexer(ElasticsearchConfiguration config) {
    this.config = config;
    client = new TextRepoElasticClient(config);
  }

  public void indexDocument(@Nonnull UUID document, @NotNull String latestVersionContent) {
    var indexRequest = new IndexRequest(config.index)
        .id(document.toString())
        .source(config.contentField, latestVersionContent);
    indexRequest(indexRequest);
  }

  void indexRequest(IndexRequest indexRequest) {
    try {
      client.getClient().index(indexRequest, RequestOptions.DEFAULT);
    } catch (IOException ex) {
      throw new RuntimeException("Could not index in Elasticsearch", ex);
    }
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() throws Exception {
    client.getClient().close();
  }

}
