package nl.knaw.huc.service.index;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.UUID;

public class ElasticDocumentIndexer implements DocumentIndexer {

  private RestHighLevelClient elasticsearchClient;
  private final String index;

  public ElasticDocumentIndexer(RestHighLevelClient elasticsearchClient, String index) {
    this.elasticsearchClient = elasticsearchClient;
    this.index = index;
  }

  public void indexDocument(@Nonnull UUID document, @NotNull String latestVersionContent) {
    var indexRequest = new IndexRequest(index)
        .id(document.toString())
        .source("content", latestVersionContent);
    try {
      elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
    } catch (IOException ex) {
      throw new RuntimeException("Could not add file to files index", ex);
    }
  }

}
