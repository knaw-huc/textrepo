package nl.knaw.huc.service.index;

import io.dropwizard.lifecycle.Managed;
import nl.knaw.huc.core.TextrepoFile;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.WebApplicationException;
import java.util.Optional;

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

  public Optional<String> index(@Nonnull TextrepoFile file, @Nonnull String latestVersionContents) {
    logger.info("Add file [{}] to index [{}]", file, config.index);
    var indexRequest = new IndexRequest(config.index)
        .id(file.getId().toString())
        .source(config.contentsField, latestVersionContents);
    final var response = indexRequest(indexRequest);
    return Optional.of(String.format("Index [%s] %s for id=%s (version: %d)",
        response.getIndex(), response.getResult().getLowercase(), response.getId(), response.getVersion()
    ));
  }

  IndexResponse indexRequest(IndexRequest indexRequest) {
    try {
      return client.getClient().index(indexRequest, RequestOptions.DEFAULT);
    } catch (Exception ex) {
      throw new WebApplicationException("Could not index in Elasticsearch", ex);
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
