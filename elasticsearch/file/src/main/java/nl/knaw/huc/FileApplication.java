package nl.knaw.huc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huc.exception.TextRepoRequestExceptionMapper;
import nl.knaw.huc.health.MappingFileHealthCheck;
import nl.knaw.huc.resources.FileResource;
import nl.knaw.huc.service.FieldsService;
import nl.knaw.huc.service.LocalDateTimeSerializer;
import nl.knaw.huc.service.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.time.format.DateTimeFormatter.ofPattern;

public class FileApplication extends Application<FileConfiguration> {

  private static final Logger log = LoggerFactory.getLogger(FileApplication.class);

  public static void main(final String[] args) throws Exception {
    new FileApplication().run(args);
    log.info("File Indexer started");
  }

  @Override
  public String getName() {
    return "File";
  }

  @Override
  public void initialize(final Bootstrap<FileConfiguration> bootstrap) {
    bootstrap.addBundle(new MultiPartBundle());
  }

  @Override
  public void run(FileConfiguration config, Environment environment) {

    var objectMapper = environment.getObjectMapper();
    objectMapper.setDateFormat(new SimpleDateFormat(config.getDateFormat()));
    objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    var module = new SimpleModule();
    module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(config.getDateFormat()));
    module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(ofPattern(config.getDateFormat())));
    objectMapper.registerModule(module);

    var jsonPath = JsonPath.using(Configuration
        .builder()
        .mappingProvider(new JacksonMappingProvider(objectMapper))
        .jsonProvider(new JacksonJsonNodeJsonProvider(objectMapper))
        .build());

    var contentsService = new FieldsService(config.getTextrepoHost(), jsonPath);
    var mappingService = new MappingService(config);
    var fileResource = new FileResource(contentsService, mappingService);
    environment.jersey().register(fileResource);
    environment.jersey().register(new TextRepoRequestExceptionMapper());
    environment.healthChecks().register("mapping file", new MappingFileHealthCheck(config));
  }

}
