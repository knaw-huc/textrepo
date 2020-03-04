package nl.knaw.huc.health;

import com.codahale.metrics.health.HealthCheck;
import nl.knaw.huc.FullTextConfiguration;

import java.io.File;

import static java.lang.String.format;

public class MappingFileHealthCheck extends HealthCheck {

  private final FullTextConfiguration config;

  public MappingFileHealthCheck(FullTextConfiguration config) {
    this.config = config;
  }

  @Override
  protected Result check() {
    if (new File(config.getMappingFile()).exists()) {
      return Result.healthy();
    }
    return Result.unhealthy(format(
        "Mapping file [%s] does not exist",
        config.getMappingFile()
    ));
  }
}
