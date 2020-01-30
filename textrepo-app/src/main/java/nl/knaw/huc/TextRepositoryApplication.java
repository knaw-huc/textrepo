package nl.knaw.huc;

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.jdbi3.bundles.JdbiExceptionsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import nl.knaw.huc.resources.rest.ContentsResource;
import nl.knaw.huc.resources.FileContentsResource;
import nl.knaw.huc.resources.FileMetadataResource;
import nl.knaw.huc.resources.FileVersionsResource;
import nl.knaw.huc.resources.FilesResource;
import nl.knaw.huc.resources.TypeResource;
import nl.knaw.huc.resources.rest.DocumentMetadataResource;
import nl.knaw.huc.resources.rest.DocumentsResource;
import nl.knaw.huc.resources.task.ImportFileResource;
import nl.knaw.huc.service.task.importfile.JdbiTaskFactory;
import nl.knaw.huc.service.ContentsService;
import nl.knaw.huc.service.JdbiDocumentMetadataService;
import nl.knaw.huc.service.JdbiDocumentService;
import nl.knaw.huc.service.JdbiFileContentsService;
import nl.knaw.huc.service.JdbiFileMetadataService;
import nl.knaw.huc.service.JdbiFileService;
import nl.knaw.huc.service.JdbiTypeService;
import nl.knaw.huc.service.JdbiVersionService;
import nl.knaw.huc.service.TypeService;
import nl.knaw.huc.service.index.CustomIndexerException;
import nl.knaw.huc.service.index.ElasticCustomIndexer;
import nl.knaw.huc.service.index.ElasticFileIndexer;
import nl.knaw.huc.service.store.JdbiContentsStorage;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Supplier;

import static com.google.common.collect.Lists.newArrayList;

public class TextRepositoryApplication extends Application<TextRepositoryConfiguration> {

  private static final Logger logger = LoggerFactory.getLogger(TextRepositoryApplication.class);

  public static void main(final String[] args) throws Exception {
    new TextRepositoryApplication().run(args);
    logger.info("TextRepository app started");
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
    Supplier<UUID> uuidGenerator = UUID::randomUUID;

    var jdbi = createJdbi(config, environment);
    var contentsStoreService = new JdbiContentsStorage(jdbi);
    var contentsService = new ContentsService(contentsStoreService);
    var metadataService = new JdbiFileMetadataService(jdbi);
    var typeService = new JdbiTypeService(jdbi);
    var customIndexers = createElasticCustomFacetIndexers(config, typeService);
    var fileIndexService = new ElasticFileIndexer(config.getElasticsearch());
    var versionService = new JdbiVersionService(jdbi, contentsService, fileIndexService, customIndexers);
    var fileService = new JdbiFileService(jdbi, typeService, versionService, metadataService, uuidGenerator);
    var fileContentsService = new JdbiFileContentsService(jdbi, contentsService, versionService, metadataService);
    var documentService = new JdbiDocumentService(jdbi);
    var documentMetadataService = new JdbiDocumentMetadataService(jdbi);

    var resources = newArrayList(
        new ContentsResource(contentsService),
        new TypeResource(typeService),
        new FileContentsResource(fileContentsService),
        new FileMetadataResource(metadataService),
        new FileVersionsResource(versionService),
        new DocumentsResource(documentService),
        new DocumentMetadataResource(documentMetadataService),
        new ImportFileResource(new JdbiTaskFactory(jdbi, uuidGenerator)),
        new FilesResource(fileService)
    );

    resources.forEach((resource) -> environment.jersey().register(resource));
    environment.lifecycle().manage(fileIndexService);
    customIndexers.forEach(ci -> environment.lifecycle().manage(ci));
  }

  private Jdbi createJdbi(TextRepositoryConfiguration config, Environment environment) {
    var factory = new JdbiFactory();
    var jdbi = factory.build(
        environment,
        config.getDataSourceFactory(),
        "postgresql"
    );
    jdbi.installPlugin(new SqlObjectPlugin());
    return jdbi;
  }

  private ArrayList<ElasticCustomIndexer> createElasticCustomFacetIndexers(
      TextRepositoryConfiguration config,
      TypeService typeService
  ) {
    var customIndexers = new ArrayList<ElasticCustomIndexer>();

    for (var customIndexerConfig : config.getCustomFacetIndexers()) {
      try {
        logger.info("Creating indexer [{}]", customIndexerConfig.elasticsearch.index);
        var customFacetIndexer = new ElasticCustomIndexer(customIndexerConfig, typeService);
        customIndexers.add(customFacetIndexer);
      } catch (CustomIndexerException ex) {
        logger.error("Could not create indexer [{}]", customIndexerConfig.elasticsearch.index, ex);
      }
    }
    return customIndexers;
  }

}
