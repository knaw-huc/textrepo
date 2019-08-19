package nl.knaw.huc.service;

import io.dropwizard.lifecycle.Managed;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * Dropwizard-managed Elasticsearch client.
 */
public class ManagedEs implements Managed {
  private RestHighLevelClient client;

  public ManagedEs(RestClientBuilder builder) {
    client = new RestHighLevelClient(builder);
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
