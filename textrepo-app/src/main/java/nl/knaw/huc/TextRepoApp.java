package nl.knaw.huc;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.jdbi3.bundles.JdbiExceptionsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import nl.knaw.huc.exceptions.MethodNotAllowedExceptionMapper;
import nl.knaw.huc.helpers.Paginator;
import nl.knaw.huc.resources.dashboard.DashboardResource;
import nl.knaw.huc.resources.rest.ContentsResource;
import nl.knaw.huc.resources.rest.DocumentFilesResource;
import nl.knaw.huc.resources.rest.DocumentMetadataResource;
import nl.knaw.huc.resources.rest.DocumentsResource;
import nl.knaw.huc.resources.rest.FileMetadataResource;
import nl.knaw.huc.resources.rest.FileVersionsResource;
import nl.knaw.huc.resources.rest.FilesResource;
import nl.knaw.huc.resources.rest.MetadataResource;
import nl.knaw.huc.resources.rest.TypesResource;
import nl.knaw.huc.resources.rest.VersionContentsResource;
import nl.knaw.huc.resources.rest.VersionsResource;
import nl.knaw.huc.resources.task.DeleteDocumentResource;
import nl.knaw.huc.resources.task.FindResource;
import nl.knaw.huc.resources.task.ImportResource;
import nl.knaw.huc.resources.task.IndexResource;
import nl.knaw.huc.resources.task.RegisterIdentifiersResource;
import nl.knaw.huc.service.contents.ContentsService;
import nl.knaw.huc.service.dashboard.JdbiDashboardService;
import nl.knaw.huc.service.datetime.LocalDateTimeParamConverterProvider;
import nl.knaw.huc.service.datetime.LocalDateTimeSerializer;
import nl.knaw.huc.service.document.JdbiDocumentService;
import nl.knaw.huc.service.document.files.JdbiDocumentFilesService;
import nl.knaw.huc.service.document.metadata.JdbiDocumentMetadataService;
import nl.knaw.huc.service.file.JdbiFileService;
import nl.knaw.huc.service.file.metadata.JdbiFileMetadataService;
import nl.knaw.huc.service.health.ElasticsearchHealthCheck;
import nl.knaw.huc.service.health.IndexerHealthCheck;
import nl.knaw.huc.service.index.Indexer;
import nl.knaw.huc.service.index.IndexerException;
import nl.knaw.huc.service.index.MappedIndexer;
import nl.knaw.huc.service.index.TextRepoElasticClient;
import nl.knaw.huc.service.logging.LoggingApplicationEventListener;
import nl.knaw.huc.service.store.JdbiContentsStorage;
import nl.knaw.huc.service.task.JdbiTaskFactory;
import nl.knaw.huc.service.type.JdbiTypeService;
import nl.knaw.huc.service.type.TypeService;
import nl.knaw.huc.service.version.JdbiVersionService;
import nl.knaw.huc.service.version.content.JdbiVersionContentsService;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toMap;

public class TextRepoApp extends Application<TextRepoConfiguration> {

  private static final Logger log = LoggerFactory.getLogger(TextRepoApp.class);

  public static void main(final String[] args) throws Exception {
    new TextRepoApp().run(args);
    log.info("Text Repository Application started");
  }

  @Override
  public String getName() {
    return "Text Repository";
  }

  @Override
  public void initialize(@Nonnull final Bootstrap<TextRepoConfiguration> bootstrap) {
    bootstrap.addBundle(new MultiPartBundle());
    bootstrap.addBundle(new JdbiExceptionsBundle());
    bootstrap.addBundle(getSwaggerBundle());
  }

  private SwaggerBundle<TextRepoConfiguration> getSwaggerBundle() {
    return new SwaggerBundle<>() {
      @Override
      protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(TextRepoConfiguration configuration) {
        return configuration.getSwaggerBundleConfiguration();
      }
    };
  }

  @Override
  public void run(TextRepoConfiguration config, Environment environment) {
    final Supplier<UUID> uuidGenerator = UUID::randomUUID;

    final var maxPayloadSize = 10 * 1024 * 1024; // TODO: arbitrary, get from configuration and figure out sane default

    var jdbi = createJdbi(config, environment);
    var contentsStoreService = new JdbiContentsStorage(jdbi);
    var contentsService = new ContentsService(contentsStoreService);
    var metadataService = new JdbiFileMetadataService(jdbi);
    var typeService = new JdbiTypeService(jdbi);

    var indexers = createElasticCustomFacetIndexers(config, typeService);
    var healthChecks = new HashMap<String, HealthCheck>();
    healthChecks.putAll(createElasticsearchHealthChecks(config));
    healthChecks.putAll(createIndexerHealthChecks(config));

    var taskBuilderFactory = new JdbiTaskFactory(jdbi, indexers)
        .withIdGenerator(uuidGenerator);
    var versionService = new JdbiVersionService(jdbi, contentsService, indexers, uuidGenerator);
    var versionContentsService = new JdbiVersionContentsService(jdbi);
    var fileService = new JdbiFileService(jdbi, versionService, uuidGenerator);
    var documentFilesService = new JdbiDocumentFilesService(jdbi);
    var documentService = new JdbiDocumentService(jdbi, uuidGenerator);
    var documentMetadataService = new JdbiDocumentMetadataService(jdbi);
    var paginator = new Paginator(config.getPagination());
    var dashboardService = new JdbiDashboardService(jdbi);

    var resources = Arrays.asList(
        new ContentsResource(contentsService),
        new TypesResource(typeService),
        new FileMetadataResource(metadataService),
        new FileVersionsResource(versionService, paginator),
        new DocumentsResource(documentService, paginator),
        new DocumentMetadataResource(documentMetadataService),
        new ImportResource(taskBuilderFactory, maxPayloadSize),
        new IndexResource(taskBuilderFactory),
        new DeleteDocumentResource(taskBuilderFactory),
        new FilesResource(fileService),
        new DocumentFilesResource(documentFilesService, paginator),
        new VersionsResource(versionService, maxPayloadSize),
        new VersionContentsResource(versionContentsService),
        new FindResource(taskBuilderFactory),
        new DashboardResource(dashboardService, paginator),
        new MetadataResource(documentMetadataService),
        new RegisterIdentifiersResource(taskBuilderFactory)
    );

    environment.jersey().register(new MethodNotAllowedExceptionMapper());
    resources.forEach((resource) -> environment.jersey().register(resource));
    healthChecks.forEach((name, check) -> environment.healthChecks().register(name, check));

    var objectMapper = environment.getObjectMapper();
    objectMapper.setDateFormat(new SimpleDateFormat(config.getDateFormat()));
    var module = new SimpleModule();
    module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(config.getDateFormat()));
    objectMapper.registerModule(module);
    environment.jersey().register(new LocalDateTimeParamConverterProvider(config.getDateFormat()));
    environment.jersey().register(new LoggingApplicationEventListener(uuidGenerator));
  }

  private Map<String, HealthCheck> createElasticsearchHealthChecks(TextRepoConfiguration config) {
    return config
        .getCustomFacetIndexers()
        .stream()
        .collect(toMap(
            ix -> ix.elasticsearch.index + "-es-index",
            ix -> new ElasticsearchHealthCheck(ix.elasticsearch.index, new TextRepoElasticClient(ix.elasticsearch)))
        );
  }

  private Map<String, HealthCheck> createIndexerHealthChecks(TextRepoConfiguration config) {
    return config
        .getCustomFacetIndexers()
        .stream()
        .collect(toMap(
            ix -> ix.elasticsearch.index + "-indexer-service",
            ix -> new IndexerHealthCheck(ix.mapping))
        );
  }

  private Jdbi createJdbi(TextRepoConfiguration config, Environment environment) {
    var factory = new JdbiFactory();
    var jdbi = factory.build(
        environment,
        config.getDataSourceFactory(),
        "postgresql"
    );
    jdbi.installPlugin(new SqlObjectPlugin());
    return jdbi;
  }

  private List<Indexer> createElasticCustomFacetIndexers(
      TextRepoConfiguration config,
      TypeService typeService
  ) {
    var customIndexers = new ArrayList<Indexer>();

    for (var customIndexerConfig : config.getCustomFacetIndexers()) {
      try {
        log.info("Create index: {}", customIndexerConfig.elasticsearch.index);
        var customFacetIndexer = new MappedIndexer(customIndexerConfig, typeService);
        customIndexers.add(customFacetIndexer);
      } catch (IndexerException ex) {
        log.error("Could not create indexer: {}", customIndexerConfig.elasticsearch.index, ex);
      }
    }
    return customIndexers;
  }

}
