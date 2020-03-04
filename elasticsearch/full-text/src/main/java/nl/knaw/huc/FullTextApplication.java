package nl.knaw.huc;

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huc.exceptions.IllegalMimetypeExceptionMapper;
import nl.knaw.huc.health.MappingFileHealthCheck;
import nl.knaw.huc.resources.FullTextResource;
import nl.knaw.huc.service.FieldsService;
import nl.knaw.huc.service.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FullTextApplication extends Application<FullTextConfiguration> {

  private static final Logger logger = LoggerFactory.getLogger(FullTextApplication.class);

  public static void main(final String[] args) throws Exception {
    new FullTextApplication().run(args);
    logger.info("Full-text app started");
  }

  @Override
  public String getName() {
    return "Autocomplete";
  }

  @Override
  public void initialize(final Bootstrap<FullTextConfiguration> bootstrap) {
    bootstrap.addBundle(new MultiPartBundle());
  }

  @Override
  public void run(FullTextConfiguration config, Environment environment) {
    var contentsService = new FieldsService(config);
    var mappingService = new MappingService(config);
    var contentsResource = new FullTextResource(contentsService, mappingService);
    environment.jersey().register(contentsResource);
    environment.healthChecks().register("mapping file", new MappingFileHealthCheck(config));
    environment.jersey().register(new IllegalMimetypeExceptionMapper());
  }

}
