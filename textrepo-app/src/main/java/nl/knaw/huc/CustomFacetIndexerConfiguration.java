package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomFacetIndexerConfiguration {
  @JsonProperty
  public String mapping;

  @JsonProperty
  public String fields;

  @JsonProperty
  public ElasticsearchConfiguration elasticsearch;
}
