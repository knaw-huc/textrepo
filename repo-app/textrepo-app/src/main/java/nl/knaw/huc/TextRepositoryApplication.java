package nl.knaw.huc;

import io.dropwizard.Application;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huc.health.TemplateHealthCheck;
import nl.knaw.huc.resources.HelloWorldResource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
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
  public void initialize(final Bootstrap<TextRepositoryConfiguration> bootstrap) {}

  @Override
  public void run(
    TextRepositoryConfiguration configuration,
    Environment environment
  ) {

    var healthCheck = new TemplateHealthCheck(
      configuration.getTemplate()
    );
    environment.healthChecks().register("template", healthCheck);

    var factory = new JdbiFactory();
    var jdbi = factory.build(
      environment,
      configuration.getDataSourceFactory(),
      "postgresql"
    );
    jdbi.installPlugin(new SqlObjectPlugin());

    var resource = new HelloWorldResource(
      configuration.getTemplate(),
      configuration.getDefaultName(),
      jdbi
    );

    environment.jersey().register(resource);
  }

}
