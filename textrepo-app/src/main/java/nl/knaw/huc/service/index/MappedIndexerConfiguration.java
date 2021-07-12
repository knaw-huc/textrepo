package nl.knaw.huc.service.index;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MappedIndexerConfiguration extends IndexerConfiguration {

  /**
   * GET Endpoint to request es index mapping that is used to create custom index
   */
  @JsonProperty
  public String mapping;

  /**
   * POST Endpoint to convert doc to json doc that matches es mapping
   */
  @JsonProperty
  public FieldsConfiguration fields;

  /**
   * Location of elasticsearch nodes
   */
  @JsonProperty
  public ElasticsearchConfiguration elasticsearch;
}
