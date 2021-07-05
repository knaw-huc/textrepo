package nl.knaw.huc.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FlywayConfiguration {

  @JsonProperty
  public boolean cleanDisabled;

  @JsonProperty
  public String[] locations;
}
