package nl.knaw.huc.resources;

import io.dropwizard.Application;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.huc.service.datetime.LocalDateTimeParamConverterProvider;
import nl.knaw.huc.TextRepositoryConfiguration;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.resources.rest.DocumentsResource;
import nl.knaw.huc.service.DocumentService;
import nl.knaw.huc.service.logging.LoggingApplicationEventListener;
import nl.knaw.huc.service.Paginator;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.LocalDateTime.now;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class LoggingTest {

  private static File logFile = new File("target/testlog.log");
  private static DocumentService documentService = mock(DocumentService.class);
  private static Paginator paginator = mock(Paginator.class);

  public static DropwizardAppExtension<TextRepositoryConfiguration> application;

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  /*
    To test logging: create test app with DocumentResource and custom config and file logger
   */
  static {
    try {
      var factory = new YamlConfigurationFactory<>(
          TextRepositoryConfiguration.class,
          Validators.newValidator(),
          Jackson.newObjectMapper(),
          "dw"
      );

      var configFile = new File("src/test/resources/logging/config.yml");
      var textRepositoryConfiguration = factory.build(configFile);
      application = new DropwizardAppExtension<>(TestApp.class, textRepositoryConfiguration);

    } catch (IOException | ConfigurationException ex) {
      throw new RuntimeException("Could not init test app", ex);
    }
  }

  @BeforeEach
  public void setUp() throws IOException {
    // Clear file manually: cannot set Append=false in config.yml
    if (logFile.exists()) {
      FileUtils.write(logFile, "", UTF_8);
    }

    reset(documentService, paginator);
  }

  @Test
  public void documentResource_shouldLogUniqueRequestIdSetByLoggingRequestEventListener_whenMultipleRequestFired()
      throws InterruptedException, IOException
  {

    var documents = new ArrayList<Document>();
    documents.add(new Document(null, null, null));
    var page = new Page<>(documents, 0, new PageParams(0, 10));
    when(documentService.getAll(anyString(), isNull(), any())).thenReturn(page);
    when(
        documentService.getAll(anyString(), isNull(), any())
    ).thenAnswer(invocation -> {
      var items = new ArrayList<Document>();
      var doc = new Document(UUID.randomUUID(), invocation.getArgument(0, String.class), now());
      items.add(doc);
      return new Page<>(items, 1, new PageParams(10, 0));
    });

    // Requests:
    var testExternalIds = Collections.synchronizedCollection(new ArrayList<String>());
    var requestsToPerform = 100;
    for (var i = 0; i < requestsToPerform; i++) {
      new Thread(() -> {
        var externalId = UUID.randomUUID().toString();
        performGet(externalId);
        testExternalIds.add(externalId);
      }).start();
    }

    while (testExternalIds.size() != requestsToPerform) {
      logger.info("Wait for {} concurrent requests to finish", requestsToPerform);
      MILLISECONDS.sleep(100);
    }

    assertThat(testExternalIds).doesNotContainNull();
    assertThat(testExternalIds).hasSize(requestsToPerform);

    var testlogFile = logFile;
    var logging = FileUtils.readFileToString(testlogFile, "UTF-8");

    testExternalIds.forEach((testExternalId) ->
        assertThat(logging).contains(testExternalId)
    );

    // Logs marking start of request (i.e. 'get documents'):
    var startGetDocumentsRegex = "DEBUG.*request=([a-f0-9-]{36}).*(get documents).*externalId=([a-f0-9-]{36})";
    var startLines = getLinesByRegex(logging, startGetDocumentsRegex);
    var startRequestIdsExternalIds = new HashMap<String, String>();
    startLines.forEach((line) -> startRequestIdsExternalIds.put(line.group(1), line.group(3)));

    // Logs marking end of a request (i.e. 'got documents'):
    var endGetDocumentsRegex = "DEBUG.*request=([a-f0-9-]{36}).*(got documents).*externalId=([a-f0-9-]{36})";
    var endLines = getLinesByRegex(logging, endGetDocumentsRegex);
    var endRequestIdsExternalIds = new HashMap<String, String>();
    startLines.forEach((line) -> endRequestIdsExternalIds.put(line.group(1), line.group(3)));

    // Test start of request is logged:
    assertThat(endLines.size()).isEqualTo(requestsToPerform);
    testExternalIds.forEach((testExternalId) -> assertThat(startRequestIdsExternalIds).containsValue(testExternalId));

    // Test end of request is logged:
    assertThat(startLines.size()).isEqualTo(requestsToPerform);
    testExternalIds.forEach((testExternalId) -> assertThat(endRequestIdsExternalIds).containsValue(testExternalId));

    // Test request ID remains linked to external ID:
    startRequestIdsExternalIds.forEach((requestId, externalId) ->
        assertThat(endRequestIdsExternalIds.get(requestId)).isEqualTo(externalId)
    );

    // Test each request gets and keeps its own ID:
    assertThat(startRequestIdsExternalIds.keySet().size()).isEqualTo(requestsToPerform);
    assertThat(endRequestIdsExternalIds.keySet().size()).isEqualTo(requestsToPerform);
    assertThat(startRequestIdsExternalIds.keySet()).containsAll(endRequestIdsExternalIds.keySet());
  }

  private List<MatchResult> getLinesByRegex(String logging, String getDocumentsRegex) {
    var pattern = Pattern.compile(getDocumentsRegex);
    var matcher = pattern.matcher(logging);
    return matcher.results().collect(toList());
  }

  private void performGet(String externalId) {
    application
        .client()
        .register(MultiPartFeature.class)
        .target("http://localhost:8765/rest/documents?externalId=" + externalId)
        .request()
        .get();
  }


  public static class TestApp extends Application<TextRepositoryConfiguration> {

    @Override
    public void initialize(Bootstrap<TextRepositoryConfiguration> bootstrap) {
    }

    @Override
    public void run(TextRepositoryConfiguration config, Environment environment) {
      environment.jersey().register(new LocalDateTimeParamConverterProvider(config.getDateFormat()));
      environment.jersey().register(new DocumentsResource(documentService, paginator));
      environment.jersey().register(new LoggingApplicationEventListener(UUID::randomUUID));
      environment.jersey().register(this);
    }

  }
}
