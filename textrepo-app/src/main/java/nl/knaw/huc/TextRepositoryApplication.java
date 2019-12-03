package nl.knaw.huc;

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.jdbi3.bundles.JdbiExceptionsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import nl.knaw.huc.resources.ContentsResource;
import nl.knaw.huc.resources.FileContentsResource;
import nl.knaw.huc.resources.FileMetadataResource;
import nl.knaw.huc.resources.FileVersionsResource;
import nl.knaw.huc.resources.FilesResource;
import nl.knaw.huc.resources.TypeResource;
import nl.knaw.huc.service.ContentsService;
import nl.knaw.huc.service.FileContentsService;
import nl.knaw.huc.service.FileService;
import nl.knaw.huc.service.JdbiMetadataService;
import nl.knaw.huc.service.JdbiTypeService;
import nl.knaw.huc.service.JdbiVersionService;
import nl.knaw.huc.service.index.ElasticCustomFacetIndexer;
import nl.knaw.huc.service.index.ElasticFileIndexer;
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
    bootstrap.addBundle(new JdbiExceptionsBundle());
    bootstrap.addBundle(getSwaggerBundle());
  }

  private SwaggerBundle<TextRepositoryConfiguration> getSwaggerBundle() {
    return new SwaggerBundle<>() {
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

    var fileIndexService = new ElasticFileIndexer(config.getElasticsearch());
    environment.lifecycle().manage(fileIndexService);

    var customIndexers = new ArrayList<ElasticCustomFacetIndexer>();
    for (var customIndexerConfig : config.getCustomFacetIndexers()) {
      var customFacetIndexer = new ElasticCustomFacetIndexer(customIndexerConfig);
      environment.lifecycle().manage(customFacetIndexer);
      customIndexers.add(customFacetIndexer);
    }

    var contentsStoreService = new JdbiContentsStorage(jdbi);
    var contentsService = new ContentsService(contentsStoreService);
    var contentsResource = new ContentsResource(contentsService);

    var metadataService = new JdbiMetadataService(jdbi);

    var versionService = new JdbiVersionService(jdbi, contentsService, fileIndexService, customIndexers);

    var typeService = new JdbiTypeService(jdbi);
    var typeResource = new TypeResource(typeService);

    var fileService = new FileService(jdbi, typeService, versionService, metadataService, UUID::randomUUID);
    var filesResource = new FilesResource(fileService);

    var fileContentsService = new FileContentsService(contentsService, versionService, metadataService);
    var fileContentsResource = new FileContentsResource(fileContentsService);

    var metadataResource = new FileMetadataResource(metadataService);

    var versionsResource = new FileVersionsResource(versionService);

    environment.jersey().register(typeResource);
    environment.jersey().register(metadataResource);
    environment.jersey().register(filesResource);
    environment.jersey().register(fileContentsResource);
    environment.jersey().register(contentsResource);
    environment.jersey().register(versionsResource);
  }

}
