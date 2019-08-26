package nl.knaw.huc;

import io.dropwizard.lifecycle.Managed;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.text.StringEscapeUtils.escapeJava;
import static org.elasticsearch.client.RestClient.builder;

/**
 * Dropwizard-managed Elasticsearch client.
 */
public class ManagedElasticsearchClient implements Managed {
  private RestHighLevelClient client;

  public ManagedElasticsearchClient(List<String> hosts) {
    var restClientBuilder = builder(hosts.stream()
                                   .map(ManagedElasticsearchClient::parseAddr)
                                   .collect(toList())
                                   .toArray(new HttpHost[hosts.size()]));
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
