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
import nl.knaw.huc.TextRepoConfiguration;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.helpers.Paginator;
import nl.knaw.huc.resources.rest.DocumentsResource;
import nl.knaw.huc.service.document.DocumentService;
import nl.knaw.huc.service.datetime.LocalDateTimeParamConverterProvider;
import nl.knaw.huc.service.logging.LoggingApplicationEventListener;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

  private static final Logger log = LoggerFactory.getLogger(LoggingTest.class);
  private static final File configFile = new File("src/test/resources/logging/config.yml");
  private static final File logFile = new File("target/testlog.log");
  private static final String endpoint = "http://localhost:8765/rest/documents";

  private static final DocumentService documentService = mock(DocumentService.class);
  private static final Paginator paginator = mock(Paginator.class);

  public static DropwizardAppExtension<TextRepoConfiguration> application;

  /*
    To test logging: create test app with DocumentResource and custom config and file logger
   */
  static {
    try {
      var factory = new YamlConfigurationFactory<>(
          TextRepoConfiguration.class,
          Validators.newValidator(),
          Jackson.newObjectMapper(),
          "dw"
      );

      var textRepoConfiguration = factory.build(configFile);
      application = new DropwizardAppExtension<>(TestApp.class, textRepoConfiguration);

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
      throws InterruptedException, IOException {

    // Mock creation of documents:
    var documents = new ArrayList<Document>();
    documents.add(new Document(null, null, null));
    var page = new Page<>(documents, 0, new PageParams(0, 10));
    when(documentService.getAll(anyString(), isNull(), any())).thenReturn(page);
    when(documentService.getAll(anyString(), isNull(), any())).thenAnswer(invocation -> {
      var items = new ArrayList<Document>();
      var doc = new Document(UUID.randomUUID(), invocation.getArgument(0, String.class), now());
      items.add(doc);
      return new Page<>(items, 1, new PageParams(10, 0));
    });

    // Perform requests:
    var testExternalIds = Collections.synchronizedCollection(new ArrayList<String>());
    var requestsToPerform = 10;
    assertThat(application.client().target(endpoint).request()).isNotNull();
    for (var i = 0; i < requestsToPerform; i++) {
      new Thread(() -> {
        var externalId = UUID.randomUUID().toString();
        performGet(externalId);
        testExternalIds.add(externalId);
      }).start();
    }

    while (testExternalIds.size() != requestsToPerform) {
      log.info("Wait for {} concurrent requests to finish", requestsToPerform);
      MILLISECONDS.sleep(100);
    }

    assertThat(testExternalIds).doesNotContainNull();
    assertThat(testExternalIds).hasSize(requestsToPerform);

    var logging = FileUtils.readFileToString(logFile, "UTF-8");

    testExternalIds.forEach((testExternalId) ->
        assertThat(logging).contains(testExternalId)
    );

    // Logs marking start of request (i.e. 'get documents'):
    var startGetDocumentsRegex = "DEBUG.*request=([a-f0-9-]{36}).*(Get documents).*externalId=([a-f0-9-]{36})";
    var startLines = getLinesByRegex(logging, startGetDocumentsRegex);
    var startRequestIdsExternalIds = new HashMap<String, String>();
    startLines.forEach((line) -> startRequestIdsExternalIds.put(line.group(1), line.group(3)));

    // Logs marking end of a request (i.e. 'got documents'):
    var endGetDocumentsRegex = "DEBUG.*request=([a-f0-9-]{36}).*(Got documents).*externalId=([a-f0-9-]{36})";
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
        .target(endpoint + "?externalId=" + externalId)
        .request()
        .get();
  }


  public static class TestApp extends Application<TextRepoConfiguration> {

    @Override
    public void initialize(Bootstrap<TextRepoConfiguration> bootstrap) {
    }

    @Override
    public void run(TextRepoConfiguration config, Environment environment) {
      environment.jersey().register(new LocalDateTimeParamConverterProvider(config.getDateFormat()));
      environment.jersey().register(new DocumentsResource(documentService, paginator));
      environment.jersey().register(new LoggingApplicationEventListener(UUID::randomUUID));
      environment.jersey().register(this);
    }

  }
}
