package nl.knaw.huc.health;

import nl.knaw.huc.FullTextConfiguration;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class MappingFileHealthCheckTest {

  @Test
  public void check_returnsHealthy_whenMappingFileExists() {
    var filename = "test-mapping-file.json";
    var path = new File("src/test/resources/" + filename).getPath();
    var config = new FullTextConfiguration();
    config.setMappingFile(path);
    var healthCheck = new MappingFileHealthCheck(config);

    var result = healthCheck.check();

    assertThat(result.isHealthy()).isEqualTo(true);
  }

  @Test
  public void check_returnsUnhealthy_whenMappingFileDoesNotExist() {
    var missingMapperFile = "missing-mapping-file.json";
    var path = new File("src/test/resources/" + missingMapperFile).getPath();
    var config = new FullTextConfiguration();
    config.setMappingFile(path);
    var healthCheck = new MappingFileHealthCheck(config);

    var result = healthCheck.check();

    assertThat(result.isHealthy()).isEqualTo(false);
  }

}
