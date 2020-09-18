package nl.knaw.huc.service.index;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestHighLevelClient;

import static java.util.stream.Collectors.toList;
import static org.elasticsearch.client.RestClient.builder;

public class TextRepoElasticClient {

  private final RestHighLevelClient client;

  public TextRepoElasticClient(ElasticsearchConfiguration config) {
    var restClientBuilder = builder(config.hosts
        .stream()
        .map(HttpHost::create)
        .collect(toList())
        .toArray(new HttpHost[config.hosts.size()]));
    client = new RestHighLevelClient(restClientBuilder);
  }

  public RestHighLevelClient getClient() {
    return client;
  }
}
