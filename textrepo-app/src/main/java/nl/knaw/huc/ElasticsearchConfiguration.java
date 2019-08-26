package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ElasticsearchConfiguration {
  /**
   * Hosts of the Elasticsearch cluster with which to connect.
   * All hosts must be in the format $hostname[:$port].
   * The port defaults to 9200.
   */
  @JsonProperty
  List<String> hosts;

  /**
   * Name of index to use.
   */
  @JsonProperty
  String index;
}
