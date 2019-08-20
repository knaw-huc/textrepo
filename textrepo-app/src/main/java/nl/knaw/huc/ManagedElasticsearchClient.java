package nl.knaw.huc;

import io.dropwizard.lifecycle.Managed;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestHighLevelClient;

import static org.elasticsearch.client.RestClient.builder;

/**
 * Dropwizard-managed Elasticsearch client.
 */
public class ManagedElasticsearchClient implements Managed {
  private RestHighLevelClient client;

  public ManagedElasticsearchClient(ElasticsearchConfiguration configuration) {
    var httpHost = new HttpHost(
        configuration.getHost(),
        configuration.getPort()
    );
    var restClientBuilder = builder(httpHost);
    client = new RestHighLevelClient(restClientBuilder);
  }

  @Override
  public void start() throws Exception {
  }

  @Override
  public void stop() throws Exception {
    client.close();
  }

  public RestHighLevelClient client() {
    return client;
  }
}
