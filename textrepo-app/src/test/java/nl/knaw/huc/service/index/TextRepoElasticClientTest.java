package nl.knaw.huc.service.index;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

public class TextRepoElasticClientTest {

  @ParameterizedTest
  @CsvSource({
      "localhost,http://localhost:9200",
      "localhost:9200,http://localhost:9200",
      "example:8080,http://example:8080",
      "0.0.0.0:8080,http://0.0.0.0:8080",
      "2001:0db8:85a3:0000:0000:8a2e:0370:7334:9200,http://2001:0db8:85a3:0000:0000:8a2e:0370:7334:9200"
  })
  public void newClient_shouldParseAddressesCorrectly(String in, String out) {
    var config = new ElasticsearchConfiguration();
    config = new ElasticsearchConfiguration();
    config.hosts = List.of(in);
    config.index = "test-index-name";
    var toTest = new TextRepoElasticClient(config);
    var host = toTest.getClient().getLowLevelClient().getNodes().get(0).getHost().toString();
    assertThat(host).isEqualTo(out);
  }

}
