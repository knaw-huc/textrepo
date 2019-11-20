package nl.knaw.huc;

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import nl.knaw.huc.resources.DocumentFilesResource;
import nl.knaw.huc.resources.DocumentsResource;
import nl.knaw.huc.resources.ContentsResource;
import nl.knaw.huc.resources.MetadataResource;
import nl.knaw.huc.resources.VersionsResource;
import nl.knaw.huc.service.DocumentContentsService;
import nl.knaw.huc.service.DocumentService;
import nl.knaw.huc.service.ContentsService;
import nl.knaw.huc.service.JdbiMetadataService;
import nl.knaw.huc.service.JdbiVersionService;
import nl.knaw.huc.service.index.ElasticCustomFacetIndexer;
import nl.knaw.huc.service.index.ElasticDocumentIndexer;
import nl.knaw.huc.service.store.JdbiContentsStorage;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
    bootstrap.addBundle(getSwaggerBundle());
  }

  private SwaggerBundle<TextRepositoryConfiguration> getSwaggerBundle() {
    return new SwaggerBundle<TextRepositoryConfiguration>() {
      @Override
      protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(TextRepositoryConfiguration configuration) {
        return configuration.getSwaggerBundleConfiguration();
      }
    };
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

    var documentIndexService = new ElasticDocumentIndexer(config.getElasticsearch());
    environment.lifecycle().manage(documentIndexService);

    var customIndexers = new ArrayList<ElasticCustomFacetIndexer>();
    for (var customIndexerConfig : config.getCustomFacetIndexers()) {
      var customFacetIndexer = new ElasticCustomFacetIndexer(customIndexerConfig);
      environment.lifecycle().manage(customFacetIndexer);
      customIndexers.add(customFacetIndexer);
    }

    var contentsStoreService = new JdbiContentsStorage(jdbi);
    var contentsService = new ContentsService(contentsStoreService);
    var filesResource = new ContentsResource(contentsService);

    var metadataService = new JdbiMetadataService(jdbi);
    var versionService = new JdbiVersionService(jdbi, contentsService, documentIndexService, customIndexers);
    var documentFileService = new DocumentContentsService(contentsService, versionService, metadataService);
    var documentService = new DocumentService(versionService, UUID::randomUUID, metadataService);
    var documentsResource = new DocumentsResource(documentService);
    var documentFilesResource = new DocumentFilesResource(documentFileService);
    var metadataResource = new MetadataResource(metadataService);
    var versionsResource = new VersionsResource(versionService);

    environment.jersey().register(metadataResource);
    environment.jersey().register(documentsResource);
    environment.jersey().register(documentFilesResource);
    environment.jersey().register(filesResource);
    environment.jersey().register(versionsResource);
  }

}
