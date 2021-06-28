package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FlywayConfig {

  @JsonProperty
  public boolean cleanDisabled;

  @JsonProperty
  public String[] locations;
}
