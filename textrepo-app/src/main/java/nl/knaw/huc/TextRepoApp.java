package nl.knaw.huc;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.jdbi3.bundles.JdbiExceptionsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import nl.knaw.huc.exceptions.MethodNotAllowedExceptionMapper;
import nl.knaw.huc.helpers.ContentsHelper;
import nl.knaw.huc.helpers.Limits;
import nl.knaw.huc.helpers.Paginator;
import nl.knaw.huc.resources.ResourcesBuilder;
import nl.knaw.huc.resources.view.TextViewerResource;
import nl.knaw.huc.resources.view.ViewBuilderFactory;
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
import nl.knaw.huc.service.version.metadata.JdbiVersionMetadataService;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
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

    var jdbi = createJdbi(config, environment);

    var dataSourceFactory = config.getDataSourceFactory();
    var configure = Flyway
        .configure()
        .dataSource(
            dataSourceFactory.getUrl(),
            dataSourceFactory.getUser(),
            dataSourceFactory.getPassword()
        ).locations(config.getFlyway().locations);
    System.out.println("locations:" + Arrays.toString(config.getFlyway().locations));
    var flyway = new Flyway(configure);
    flyway.migrate();

    var contentsStoreService = new JdbiContentsStorage(jdbi);
    var contentsService = new ContentsService(contentsStoreService);
    var typeService = new JdbiTypeService(jdbi);

    var indexers = createElasticCustomFacetIndexers(config, typeService);
    var healthChecks = new HashMap<String, HealthCheck>();
    healthChecks.putAll(createElasticsearchHealthChecks(config));
    healthChecks.putAll(createIndexerHealthChecks(config));

    var limits = config.getResourceLimits();
    var contentDecompressionLimit = limits.contentDecompressionLimit * Limits.BYTES_PER_KB;
    var versionService = new JdbiVersionService(jdbi, contentsService, indexers, uuidGenerator);

    var viewBuilderFactory = createViewBuilderFactory();

    var resources = new ResourcesBuilder(config)
        .contentsService(contentsService)
        .contentsHelper(new ContentsHelper(contentDecompressionLimit))
        .documentFilesService(new JdbiDocumentFilesService(jdbi))
        .dashboardService(new JdbiDashboardService(jdbi))
        .documentService(new JdbiDocumentService(jdbi, uuidGenerator))
        .documentMetadataService(new JdbiDocumentMetadataService(jdbi))
        .fileService(new JdbiFileService(jdbi, versionService, uuidGenerator))
        .fileMetadataService(new JdbiFileMetadataService(jdbi))
        .paginator(new Paginator(config.getPagination()))
        .taskBuilderFactory(new JdbiTaskFactory(jdbi, indexers)
            .withIdGenerator(uuidGenerator))
        .typeService(typeService)
        .versionContentsService(new JdbiVersionContentsService(jdbi))
        .versionMetadataService(new JdbiVersionMetadataService(jdbi))
        .versionService(versionService)
        .viewBuilderFactory(viewBuilderFactory)
        .build();

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

  /**
   * Creates a factory containing a mapping: viewname -> constructor method of Jersey Subresource
   *
   * @return factory of registered ViewBuilders, aka Jersey sub-resources
   */
  private ViewBuilderFactory createViewBuilderFactory() {
    var viewBuilderFactory = new ViewBuilderFactory();
    viewBuilderFactory.register("text", TextViewerResource::new);
    // TODO: register more ViewResource subclasses
    return viewBuilderFactory;
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
    jdbi.installPlugin(new PostgresPlugin());
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
