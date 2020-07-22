package nl.knaw.huc.service.index;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestHighLevelClient;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.text.StringEscapeUtils.escapeJava;
import static org.elasticsearch.client.RestClient.builder;

public class TextRepoElasticClient {

  private final RestHighLevelClient client;

  public TextRepoElasticClient(ElasticsearchConfiguration config) {
    var restClientBuilder = builder(config.hosts
        .stream()
        .map(TextRepoElasticClient::parseAddr)
        .collect(toList())
        .toArray(new HttpHost[config.hosts.size()]));
    client = new RestHighLevelClient(restClientBuilder);
  }

  private static HttpHost parseAddr(String addr) {
    var port = 9200;

    var colon = addr.lastIndexOf(':');
    if (colon >= 0) {
      var after = addr.substring(colon + 1);
      // TODO: see https://jira.socialhistoryservices.org/browse/TT-588
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

  public RestHighLevelClient getClient() {
    return client;
  }
}
