package nl.knaw.huc.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class FlywayConfiguration {

  @Valid
  @NotNull
  public boolean cleanDisabled;

  @Valid
  @NotNull
  public String[] locations;

}
