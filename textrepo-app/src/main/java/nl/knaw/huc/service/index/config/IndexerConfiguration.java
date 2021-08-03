package nl.knaw.huc.service.index.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class IndexerConfiguration {

  /**
   * Name of indexer
   */
  @JsonProperty
  public String name;

  /**
   * Location of elasticsearch nodes
   */
  @JsonProperty
  public ElasticsearchConfiguration elasticsearch;

}
