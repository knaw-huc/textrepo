package nl.knaw.huc.service.index.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ElasticsearchConfiguration {
  /**
   * Hosts of the Elasticsearch cluster to connect to.
   */
  @JsonProperty
  public List<String> hosts;

  /**
   * Name of index to use.
   */
  @JsonProperty
  public String index;

  /**
   * Field in which to store version contents.
   */
  @JsonProperty
  public String contentsField;
}
