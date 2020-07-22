package nl.knaw.huc;

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huc.exceptions.IllegalMimetypeExceptionMapper;
import nl.knaw.huc.health.MappingFileHealthCheck;
import nl.knaw.huc.resources.AutocompleteResource;
import nl.knaw.huc.service.FieldsService;
import nl.knaw.huc.service.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutocompleteApplication extends Application<AutocompleteConfiguration> {

  private static final Logger log = LoggerFactory.getLogger(AutocompleteApplication.class);

  public static void main(final String[] args) throws Exception {
    new AutocompleteApplication().run(args);
    log.info("Autocomplete app started");
  }

  @Override
  public String getName() {
    return "Autocomplete";
  }

  @Override
  public void initialize(final Bootstrap<AutocompleteConfiguration> bootstrap) {
    bootstrap.addBundle(new MultiPartBundle());
  }

  @Override
  public void run(AutocompleteConfiguration config, Environment environment) {
    var contentsService = new FieldsService(config);
    var mappingService = new MappingService(config);
    var contentsResource = new AutocompleteResource(contentsService, mappingService);

    environment.jersey().register(new IllegalMimetypeExceptionMapper());
    environment.jersey().register(contentsResource);

    environment.healthChecks().register("mapping file", new MappingFileHealthCheck(config));
  }

}
