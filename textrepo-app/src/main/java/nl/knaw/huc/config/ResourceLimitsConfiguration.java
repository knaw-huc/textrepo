package nl.knaw.huc.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceLimitsConfiguration {
  @JsonProperty
  public int contentDecompressionLimit;
}
