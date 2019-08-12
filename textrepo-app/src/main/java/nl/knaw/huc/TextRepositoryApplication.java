package nl.knaw.huc;

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huc.db.FileDAO;
import nl.knaw.huc.resources.FilesResource;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextRepositoryApplication extends Application<TextRepositoryConfiguration> {

  private static final Logger logger = LoggerFactory.getLogger(TextRepositoryApplication.class);

  public static void main(final String[] args) throws Exception {
    new TextRepositoryApplication().run(args);
    logger.info("App started");
  }

  @Override
  public String getName() {
    return "Text Repository";
  }

  @Override
  public void initialize(final Bootstrap<TextRepositoryConfiguration> bootstrap) {
    bootstrap.addBundle(new MultiPartBundle());
  }

  @Override
  public void run(
    TextRepositoryConfiguration configuration,
    Environment environment
  ) {

    var factory = new JdbiFactory();
    var jdbi = factory.build(
      environment,
      configuration.getDataSourceFactory(),
      "postgresql"
    );
    jdbi.installPlugin(new SqlObjectPlugin());

    var resource = new FilesResource(jdbi);

    environment.jersey().register(resource);
  }

}
