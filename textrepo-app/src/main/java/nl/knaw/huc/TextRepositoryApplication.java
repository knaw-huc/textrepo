package nl.knaw.huc;

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huc.resources.DocumentsResource;
import nl.knaw.huc.resources.FilesResource;
import nl.knaw.huc.resources.TestResource;
import nl.knaw.huc.service.FileIndexService;
import nl.knaw.huc.service.JdbiDocumentService;
import nl.knaw.huc.service.JdbiFileService;
import org.apache.http.HttpHost;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.elasticsearch.client.RestClient.builder;

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
  public void run(TextRepositoryConfiguration configuration, Environment environment) {
    var factory = new JdbiFactory();
    var jdbi = factory.build(
            environment,
            configuration.getDataSourceFactory(),
            "postgresql"
    );
    jdbi.installPlugin(new SqlObjectPlugin());

    var indexer = new FileIndexService(
      builder(new HttpHost("elasticsearch", 9200, "http"))
    );

    var filesResource = new FilesResource(new JdbiFileService(jdbi), indexer);
    var documentsResource = new DocumentsResource(new JdbiDocumentService(jdbi, UUID::randomUUID), indexer);

    var testResource = new TestResource(jdbi);

    environment.jersey().register(documentsResource);
    environment.jersey().register(filesResource);
    environment.jersey().register(testResource);
  }

}
