package nl.knaw.huc.service.index;

import nl.knaw.huc.api.TextRepoFile;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ElasticFileIndexer implements FileIndexer {

  private RestHighLevelClient elasticsearchClient;

  public ElasticFileIndexer(RestHighLevelClient elasticsearchClient) {
    this.elasticsearchClient = elasticsearchClient;
  }

  public void indexFile(TextRepoFile file) {
    var content = file.getContent();
    var indexRequest = new IndexRequest("files")
      .id(file.getSha224())
      .source("content", new String(content, UTF_8));
    try {
      elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
    } catch (IOException ex) {
      throw new RuntimeException("Could not add file to files index", ex);
    }
  }

}
