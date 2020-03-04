package nl.knaw.huc.service;

import nl.knaw.huc.FullTextConfiguration;
import nl.knaw.huc.api.Properties;
import nl.knaw.huc.exceptions.IllegalMimetypeException;
import org.apache.commons.io.IOUtils;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

public class FieldsService {

  private FullTextConfiguration config;

  public FieldsService(FullTextConfiguration config) {
    this.config = config;
  }

  public Properties createFields(
      @NotNull InputStream inputStream,
      String mimetype
  ) {
    var supportedMimetypes = config.getSupportedMimetypes();
    if (!supportedMimetypes.contains(mimetype)) {
      throw new IllegalMimetypeException(format(
          "Unexpected mimetype: got [%s] but should be one of [%s]",
          mimetype, supportedMimetypes.stream().map(mt -> "" + mt).collect(joining(", ")))
      );
    }
    return fromTxt(inputStream);
  }

  private Properties fromTxt(@NotNull InputStream inputStream) {
    String text;
    try {
      text = IOUtils.toString(inputStream, UTF_8);
    } catch (IOException e) {
      throw new IllegalArgumentException("Input stream could not be converted to string");
    }
    return new Properties(text);
  }

}
