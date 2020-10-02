package nl.knaw.huc;

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huc.health.MappingFileHealthCheck;
import nl.knaw.huc.resources.FileResource;
import nl.knaw.huc.service.FieldsService;
import nl.knaw.huc.service.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileApplication extends Application<FileConfiguration> {

  private static final Logger log = LoggerFactory.getLogger(FileApplication.class);

  public static void main(final String[] args) throws Exception {
    new FileApplication().run(args);
    log.info("File app started");
  }

  @Override
  public String getName() {
    return "File";
  }

  @Override
  public void initialize(final Bootstrap<FileConfiguration> bootstrap) {
    bootstrap.addBundle(new MultiPartBundle());
  }

  @Override
  public void run(FileConfiguration config, Environment environment) {
    var contentsService = new FieldsService();
    var mappingService = new MappingService(config);
    var contentsResource = new FileResource(contentsService, mappingService);
    environment.jersey().register(contentsResource);
    environment.healthChecks().register("mapping file", new MappingFileHealthCheck(config));
  }

}
