package nl.knaw.huc;

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.jdbi3.bundles.JdbiExceptionsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import nl.knaw.huc.exceptions.MethodNotAllowedExceptionMapper;
import nl.knaw.huc.resources.rest.ContentsResource;
import nl.knaw.huc.resources.rest.DocumentFilesResource;
import nl.knaw.huc.resources.rest.DocumentMetadataResource;
import nl.knaw.huc.resources.rest.DocumentsResource;
import nl.knaw.huc.resources.rest.FileMetadataResource;
import nl.knaw.huc.resources.rest.FileVersionsResource;
import nl.knaw.huc.resources.rest.FilesResource;
import nl.knaw.huc.resources.rest.TypesResource;
import nl.knaw.huc.resources.rest.VersionContentsResource;
import nl.knaw.huc.resources.rest.VersionsResource;
import nl.knaw.huc.resources.task.DeleteDocumentResource;
import nl.knaw.huc.resources.task.FindResource;
import nl.knaw.huc.resources.task.ImportResource;
import nl.knaw.huc.resources.task.IndexResource;
import nl.knaw.huc.service.ContentsService;
import nl.knaw.huc.service.JdbiDocumentFilesService;
import nl.knaw.huc.service.JdbiDocumentMetadataService;
import nl.knaw.huc.service.JdbiDocumentService;
import nl.knaw.huc.service.JdbiFileContentsService;
import nl.knaw.huc.service.JdbiFileMetadataService;
import nl.knaw.huc.service.JdbiFileService;
import nl.knaw.huc.service.JdbiTypeService;
import nl.knaw.huc.service.JdbiVersionContentsService;
import nl.knaw.huc.service.JdbiVersionService;
import nl.knaw.huc.service.Paginator;
import nl.knaw.huc.service.TypeService;
import nl.knaw.huc.service.index.IndexerException;
import nl.knaw.huc.service.index.MappedIndexer;
import nl.knaw.huc.service.store.JdbiContentsStorage;
import nl.knaw.huc.service.task.JdbiTaskFactory;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

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
  public void initialize(@Nonnull final Bootstrap<TextRepositoryConfiguration> bootstrap) {
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
    final Supplier<UUID> uuidGenerator = UUID::randomUUID;

    final int maxPayloadSize = 10 * 1024 * 1024; // TODO: arbitrary, get from configuration and figure out sane default

    var jdbi = createJdbi(config, environment);
    var contentsStoreService = new JdbiContentsStorage(jdbi);
    var contentsService = new ContentsService(contentsStoreService);
    var metadataService = new JdbiFileMetadataService(jdbi);
    var typeService = new JdbiTypeService(jdbi);
    var customIndexers = createElasticCustomFacetIndexers(config, typeService);
    var taskBuilderFactory = new JdbiTaskFactory(jdbi)
        .withIdGenerator(uuidGenerator);
    var versionService = new JdbiVersionService(jdbi, contentsService, customIndexers, uuidGenerator);
    var versionContentsService = new JdbiVersionContentsService(jdbi);
    var fileService = new JdbiFileService(jdbi, typeService, versionService, metadataService, uuidGenerator);
    var documentFilesService = new JdbiDocumentFilesService(jdbi);
    var fileContentsService = new JdbiFileContentsService(jdbi, contentsService, versionService, metadataService);
    var documentService = new JdbiDocumentService(jdbi, uuidGenerator);
    var documentMetadataService = new JdbiDocumentMetadataService(jdbi);
    var paginator = new Paginator(config.getPagination());

    var resources = Arrays.asList(
        new ContentsResource(contentsService),
        new TypesResource(typeService),
        new FileMetadataResource(metadataService),
        new FileVersionsResource(versionService, paginator),
        new DocumentsResource(documentService, paginator),
        new DocumentMetadataResource(documentMetadataService),
        new ImportResource(taskBuilderFactory, maxPayloadSize),
        new IndexResource(taskBuilderFactory),
        new FindResource(taskBuilderFactory),
        new DeleteDocumentResource(taskBuilderFactory),
        new FilesResource(fileService),
        new DocumentFilesResource(documentFilesService, paginator),
        new VersionsResource(versionService, maxPayloadSize),
        new VersionContentsResource(versionContentsService)
    );

    environment.jersey().register(new MethodNotAllowedExceptionMapper());
    resources.forEach((resource) -> environment.jersey().register(resource));
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

  private ArrayList<MappedIndexer> createElasticCustomFacetIndexers(
      TextRepositoryConfiguration config,
      TypeService typeService
  ) {
    var customIndexers = new ArrayList<MappedIndexer>();

    for (var customIndexerConfig : config.getCustomFacetIndexers()) {
      try {
        logger.info("Creating indexer [{}]", customIndexerConfig.elasticsearch.index);
        var customFacetIndexer = new MappedIndexer(customIndexerConfig, typeService);
        customIndexers.add(customFacetIndexer);
      } catch (IndexerException ex) {
        logger.error("Could not create indexer [{}]", customIndexerConfig.elasticsearch.index, ex);
      }
    }
    return customIndexers;
  }

}
