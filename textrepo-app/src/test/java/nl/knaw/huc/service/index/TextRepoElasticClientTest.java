package nl.knaw.huc.service.index;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TextRepoElasticClientTest {

  @ParameterizedTest
  @CsvSource({
      "localhost:9200,http://localhost:9200",
      "localhost,http://localhost",
      "0.0.0.0:8080,http://0.0.0.0:8080",
      "example:8080,http://example:8080",
      "example,http://example",
      "http://www.example.com:9200,http://www.example.com:9200",
      "https://www.example.com:9200,https://www.example.com:9200",
      "2001:0db8:85a3:0000:0000:8a2e:0370:7334:9200,http://2001:0db8:85a3:0000:0000:8a2e:0370:7334:9200",
  })
  public void newClient_shouldParseAddressesCorrectly(String in, String expected) {
    var config = new ElasticsearchConfiguration();
    config.hosts = List.of(in);
    var client = new TextRepoElasticClient(config);
    var toTest = client.getClient().getLowLevelClient().getNodes().get(0).getHost().toString();
    assertThat(toTest).isEqualTo(expected);
  }

}
