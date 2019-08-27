package nl.knaw.huc.service.index;

import io.dropwizard.lifecycle.Managed;
import nl.knaw.huc.ElasticsearchConfiguration;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.text.StringEscapeUtils.escapeJava;
import static org.elasticsearch.client.RestClient.builder;


public class ElasticDocumentIndexer implements DocumentIndexer, Managed {
  private RestHighLevelClient client;
  private final String index;

  public ElasticDocumentIndexer(ElasticsearchConfiguration config) {
    var restClientBuilder = builder(config.hosts
        .stream()
        .map(ElasticDocumentIndexer::parseAddr)
        .collect(toList())
        .toArray(new HttpHost[config.hosts.size()]));
    client = new RestHighLevelClient(restClientBuilder);
    index = config.index;
  }

  public void indexDocument(@Nonnull UUID document, @NotNull String latestVersionContent) {
    var indexRequest = new IndexRequest(index)
        .id(document.toString())
        .source("content", latestVersionContent);
    try {
      client.index(indexRequest, RequestOptions.DEFAULT);
    } catch (IOException ex) {
      throw new RuntimeException("Could not index document in Elasticsearch", ex);
    }
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() throws Exception {
    client.close();
  }

  private static HttpHost parseAddr(String addr) {
    int port = 9200;

    int colon = addr.lastIndexOf(':');
    if (colon >= 0) {
      String after = addr.substring(colon + 1);
      if (!after.matches("[0-9:]+\\]")) {
        try {
          port = Integer.parseInt(after);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException(
              String.format("Invalid port number \"%s\"", escapeJava(after)), e);
        }
        addr = addr.substring(0, colon);
      }
    }

    return new HttpHost(addr, port);
  }
}
