package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaginationConfiguration {

  @JsonProperty
  public int defaultLimit;

  @JsonProperty
  public int defaultOffset;


}
