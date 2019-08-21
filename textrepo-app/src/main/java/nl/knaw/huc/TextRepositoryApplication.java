package nl.knaw.huc;

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huc.resources.DocumentsResource;
import nl.knaw.huc.resources.FilesResource;
import nl.knaw.huc.resources.MetadataResource;
import nl.knaw.huc.service.DocumentService;
import nl.knaw.huc.service.FileService;
import nl.knaw.huc.service.JdbiMetadataService;
import nl.knaw.huc.service.JdbiVersionService;
import nl.knaw.huc.service.index.ElasticDocumentIndexer;
import nl.knaw.huc.service.store.JdbiFileStorage;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

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
  public void run(TextRepositoryConfiguration config, Environment environment) {
    var factory = new JdbiFactory();
    var jdbi = factory.build(
        environment,
        config.getDataSourceFactory(),
        "postgresql"
    );
    jdbi.installPlugin(new SqlObjectPlugin());

    var managedEsClient = new ManagedElasticsearchClient(config.getElasticsearch());
    var documentIndexService = new ElasticDocumentIndexer(managedEsClient.client());
    var fileStoreService = new JdbiFileStorage(jdbi);
    var fileService = new FileService(fileStoreService);
    var filesResource = new FilesResource(fileService);

    var metadataService = new JdbiMetadataService(jdbi);
    var versionService = new JdbiVersionService(jdbi, fileService, documentIndexService);
    var documentService = new DocumentService(fileService, metadataService, versionService, UUID::randomUUID);
    var documentsResource = new DocumentsResource(documentService);
    var metadataResource = new MetadataResource(metadataService);

    environment.jersey().register(metadataResource);
    environment.jersey().register(documentsResource);
    environment.jersey().register(filesResource);
  }

}
