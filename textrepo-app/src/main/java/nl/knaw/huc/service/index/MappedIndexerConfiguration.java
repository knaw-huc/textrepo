package nl.knaw.huc.service.index;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MappedIndexerConfiguration extends IndexerConfiguration {

  /**
   * GET Endpoint to request es index mapping that is used to create custom index
   */
  @JsonProperty
  public String mapping;

  /**
   * GET Endpoint to request es index types containing the mimetypes (and their 'subtypes')
   * that the indexer accepts and knows how to convert at its fields-endpoint
   */
  @JsonProperty
  public String types;

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
