package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huc.exception.TextRepoRequestExceptionMapper;
import nl.knaw.huc.health.MappingFileHealthCheck;
import nl.knaw.huc.resources.FileResource;
import nl.knaw.huc.service.FieldsService;
import nl.knaw.huc.service.JsonPathFactory;
import nl.knaw.huc.service.LocalDateTimeSerializer;
import nl.knaw.huc.service.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.time.format.DateTimeFormatter.ofPattern;

public class FileIndexer extends Application<FileConfiguration> {

  private static final Logger log = LoggerFactory.getLogger(FileIndexer.class);

  public static void main(final String[] args) throws Exception {
    new FileIndexer().run(args);
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
    objectMapper.setSerializationInclusion(NON_NULL);

    var jsonPath = JsonPathFactory.withJackson(objectMapper);

    var fieldsService = new FieldsService(config.getTextrepoHost(), jsonPath, config.getPageSize());
    var mappingService = new MappingService(config);
    var fileResource = new FileResource(fieldsService, mappingService);
    environment.jersey().register(fileResource);
    environment.jersey().register(new TextRepoRequestExceptionMapper());
    environment.healthChecks().register("mapping file", new MappingFileHealthCheck(config));
  }

}
