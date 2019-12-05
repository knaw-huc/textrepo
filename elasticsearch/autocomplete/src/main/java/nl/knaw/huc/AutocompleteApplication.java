package nl.knaw.huc;

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huc.exceptions.IllegalArgumentExceptionMapper;
import nl.knaw.huc.resources.AutocompleteResource;
import nl.knaw.huc.service.FieldsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutocompleteApplication extends Application<AutocompleteConfiguration> {

  private static final Logger logger = LoggerFactory.getLogger(AutocompleteApplication.class);

  public static void main(final String[] args) throws Exception {
    new AutocompleteApplication().run(args);
    logger.info("Autocomplete app started");
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
    var contentsResource = new AutocompleteResource(contentsService);

    environment.jersey().register(new IllegalArgumentExceptionMapper());
    environment.jersey().register(contentsResource);
  }

}
