package nl.knaw.huc.service.index;

import io.dropwizard.lifecycle.Managed;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.UUID;

public class ElasticFileIndexer implements FileIndexer, Managed {

  private TextRepoElasticClient client;
  private final ElasticsearchConfiguration config;

  public ElasticFileIndexer(ElasticsearchConfiguration config) {
    this.config = config;
    client = new TextRepoElasticClient(config);
  }

  public void indexFile(@Nonnull UUID fileId, @Nonnull String latestVersionContent) {
    var indexRequest = new IndexRequest(config.index)
        .id(fileId.toString())
        .source(config.contentField, latestVersionContent);
    indexRequest(indexRequest);
  }

  IndexResponse indexRequest(IndexRequest indexRequest) {
    try {
      return client.getClient().index(indexRequest, RequestOptions.DEFAULT);
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
