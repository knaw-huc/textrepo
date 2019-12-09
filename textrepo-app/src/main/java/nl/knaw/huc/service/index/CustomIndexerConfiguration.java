package nl.knaw.huc.service.index;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CustomIndexerConfiguration {

  /**
   * GET Endpoint to request es index mapping that is used to create custom index
   */
  @JsonProperty
  public String mapping;

  /**
   * POST Endpoint to convert doc to json doc that matches es mapping
   */
  @JsonProperty
  public String fields;

  /**
   * List of mimetypes that should be indexed
   */
  @JsonProperty
  public List<String> mimetypes;

  /**
   * Location of elasticsearch nodes
   */
  @JsonProperty
  public ElasticsearchConfiguration elasticsearch;
}
