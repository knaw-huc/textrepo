package nl.knaw.huc.service.index;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomFacetIndexerConfiguration {

  /**
   * GET Endpoint to request es7 index mapping that is used to create custom index
   */
  @JsonProperty
  public String mapping;

  /**
   * POST Endpoint to convert doc to json doc that matches es7 mapping
   */
  @JsonProperty
  public String fields;

  /**
   * Location of elasticsearch nodes
   */
  @JsonProperty
  public ElasticsearchConfiguration elasticsearch;
}
