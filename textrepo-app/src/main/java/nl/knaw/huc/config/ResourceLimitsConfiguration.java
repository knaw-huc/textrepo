package nl.knaw.huc.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ResourceLimitsConfiguration {
  @Valid
  @NotNull
  public int contentDecompressionLimit;

}
