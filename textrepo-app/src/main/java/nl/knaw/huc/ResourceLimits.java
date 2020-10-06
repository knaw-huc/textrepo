package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceLimits {
  @JsonProperty
  public int contentDecompressionLimit;
}
