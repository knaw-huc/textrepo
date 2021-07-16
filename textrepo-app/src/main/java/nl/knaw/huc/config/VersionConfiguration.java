package nl.knaw.huc.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VersionConfiguration {

  @JsonProperty
  public String tag;

  @JsonProperty
  public String commit;

}
