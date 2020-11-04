package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceLimitsConfiguration {
  @JsonProperty
  public int contentDecompressionLimit;
}
