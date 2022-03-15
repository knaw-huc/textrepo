package nl.knaw.huc;

import static java.util.stream.Collectors.toMap;

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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import nl.knaw.huc.config.TextRepoConfiguration;
import nl.knaw.huc.exceptions.MethodNotAllowedExceptionMapper;
import nl.knaw.huc.helpers.ContentsHelper;
import nl.knaw.huc.helpers.Limits;
import nl.knaw.huc.helpers.Paginator;
import nl.knaw.huc.resources.ResourcesBuilder;
import nl.knaw.huc.resources.view.SegmentViewerResource;
import nl.knaw.huc.resources.view.TextViewerResource;
import nl.knaw.huc.resources.view.ViewBuilderFactory;
import nl.knaw.huc.resources.view.XmlViewerResource;
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
import nl.knaw.huc.service.index.EsIndexClient;
import nl.knaw.huc.service.index.IndexerClient;
import nl.knaw.huc.service.index.IndexerWithMappingClient;
import nl.knaw.huc.service.index.JdbiIndexService;
import nl.knaw.huc.service.logging.LoggingApplicationEventListener;
import nl.knaw.huc.service.store.JdbiContentsStorage;
import nl.knaw.huc.service.task.JdbiTaskFactory;
import nl.knaw.huc.service.type.JdbiTypeService;
import nl.knaw.huc.service.version.JdbiVersionService;
import nl.knaw.huc.service.version.content.JdbiVersionContentsService;
import nl.knaw.huc.service.version.metadata.JdbiVersionMetadataService;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
          TextRepoConfiguration configuration) {
        return configuration.getSwaggerBundleConfiguration();
      }
    };
  }

  @Override
  public void run(TextRepoConfiguration config, Environment environment) {
    final Supplier<UUID> uuidGenerator = UUID::randomUUID;

    var jdbi = createJdbi(config, environment);

    var dataSourceFactory = config.getDataSourceFactory();
    var flywayConfig = Flyway
        .configure()
        .dataSource(
            dataSourceFactory.getUrl(),
            dataSourceFactory.getUser(),
            dataSourceFactory.getPassword()
        ).locations(config.getFlyway().locations);
    var flyway = new Flyway(flywayConfig);
    flyway.migrate();

    var contentsStoreService = new JdbiContentsStorage(jdbi);
    var contentsService = new ContentsService(contentsStoreService);
    var typeService = new JdbiTypeService(jdbi);

    var indexers = createIndexers(config);
    var indices = createIndexClients(config);
    var indexService = new JdbiIndexService(indexers, indices, jdbi);

    var healthChecks = new HashMap<String, HealthCheck>();
    healthChecks.putAll(createElasticsearchHealthChecks(config));
    healthChecks.putAll(createIndexerHealthChecks(config));

    var limits = config.getResourceLimits();
    var contentDecompressionLimit = limits.contentDecompressionLimit * Limits.BYTES_PER_KB;
    var versionService = new JdbiVersionService(jdbi, contentsService, uuidGenerator, indexService);

    var viewBuilderFactory = createViewBuilderFactory();

    var resources = new ResourcesBuilder(config)
        .contentsService(contentsService)
        .contentsHelper(new ContentsHelper(contentDecompressionLimit))
        .documentFilesService(new JdbiDocumentFilesService(jdbi))
        .dashboardService(new JdbiDashboardService(jdbi))
        .documentService(new JdbiDocumentService(jdbi, uuidGenerator))
        .documentMetadataService(new JdbiDocumentMetadataService(jdbi))
        .fileService(new JdbiFileService(jdbi, uuidGenerator, indexService))
        .fileMetadataService(new JdbiFileMetadataService(jdbi))
        .paginator(new Paginator(config.getPagination()))
        .taskBuilderFactory(new JdbiTaskFactory(jdbi, indexService)
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
   * Creates a factory containing a mapping: view name -> constructor method of Jersey Subresource.
   *
   * @return factory of registered ViewBuilders, aka Jersey sub-resources
   */
  private ViewBuilderFactory createViewBuilderFactory() {
    var viewBuilderFactory = new ViewBuilderFactory();

    viewBuilderFactory.register("text", TextViewerResource::new);

    // XmlViewer returns a JSON list of XML snippets. No contents helper is used to compress, e.g
    // . a whole file.
    // If more views get added here that also don't need contents helper, or perhaps need
    // different parameters,
    // we should refactor this to get rid of the "throw contentsHelper away" kludge used here.
    // Let's add first build up some more experience by implementing more viewers, then tackle this.
    viewBuilderFactory.register("xml",
        (contents, contentsHelper) -> new XmlViewerResource(contents));

    viewBuilderFactory.register("segments",
        (contents, contentsHelper) -> new SegmentViewerResource(contents));

    return viewBuilderFactory;
  }

  private Map<String, HealthCheck> createElasticsearchHealthChecks(TextRepoConfiguration config) {
    return config
        .getIndexers()
        .stream()
        .collect(toMap(
            esConfig -> esConfig.elasticsearch.index + "-es-index",
            esConfig -> new ElasticsearchHealthCheck(new EsIndexClient(esConfig.elasticsearch)))
        );
  }

  private Map<String, HealthCheck> createIndexerHealthChecks(TextRepoConfiguration config) {
    return config
        .getIndexers()
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

  private List<IndexerClient> createIndexers(
      TextRepoConfiguration config
  ) {
    var indexers = new ArrayList<IndexerClient>();

    for (var customIndexerConfig : config.getIndexers()) {
      log.info("Create index: {}", customIndexerConfig.elasticsearch.index);
      var indexer = new IndexerWithMappingClient(customIndexerConfig);
      indexers.add(indexer);
    }
    return indexers;
  }

  private List<EsIndexClient> createIndexClients(TextRepoConfiguration config) {
    return config
        .getIndexers()
        .stream()
        .map(indexer -> new EsIndexClient(indexer.elasticsearch))
        .collect(Collectors.toList());
  }

}
