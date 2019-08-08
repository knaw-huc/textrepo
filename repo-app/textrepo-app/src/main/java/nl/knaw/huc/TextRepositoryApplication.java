package nl.knaw.huc;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huc.health.TemplateHealthCheck;
import nl.knaw.huc.resources.HelloWorldResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextRepositoryApplication extends Application<TextRepositoryConfiguration> {

  private static final Logger logger = LoggerFactory.getLogger(TextRepositoryApplication.class);

  public static void main(final String[] args) throws Exception {
    new TextRepositoryApplication().run(args);
    var testJava11 = "Hello world";
    logger.info(testJava11);
  }

  @Override
  public String getName() {
    return "Text Repository";
  }

  @Override
  public void initialize(final Bootstrap<TextRepositoryConfiguration> bootstrap) {
    // TODO: application initialization
  }

  @Override
  public void run(final TextRepositoryConfiguration configuration,
                  final Environment environment) {
    var resource = new HelloWorldResource(
      configuration.getTemplate(),
      configuration.getDefaultName()
    );

    var healthCheck = new TemplateHealthCheck(
      configuration.getTemplate()
    );
    environment.healthChecks().register("template", healthCheck);

    environment.jersey().register(resource);
  }

}
