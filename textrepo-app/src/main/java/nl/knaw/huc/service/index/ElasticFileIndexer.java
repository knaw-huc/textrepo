package nl.knaw.huc.service.index;

import io.dropwizard.lifecycle.Managed;
import nl.knaw.huc.core.TextrepoFile;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Default indexer indexing all files as plain text
 */
public class ElasticFileIndexer implements FileIndexer, Managed {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private TextRepoElasticClient client;
  private final ElasticsearchConfiguration config;

  public ElasticFileIndexer(ElasticsearchConfiguration config) {
    this.config = config;
    client = new TextRepoElasticClient(config);
  }

  public void indexFile(@Nonnull TextrepoFile file, @Nonnull String latestVersionContent) {
    logger.info("Add file [{}] to index [{}]", file, config.index);
    var indexRequest = new IndexRequest(config.index)
        .id(file.getId().toString())
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
