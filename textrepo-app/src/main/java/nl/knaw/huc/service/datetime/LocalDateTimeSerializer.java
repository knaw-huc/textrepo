package nl.knaw.huc.service.datetime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofPattern;

/**
 * Converts LocalDateTime result to json datetime format
 *
 * <p>Datetime pattern is configured in config.yml
 */
public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

  private final String dateFormat;

  public LocalDateTimeSerializer(String dateFormat) {
    this.dateFormat = dateFormat;
  }

  @Override
  public void serialize(
      LocalDateTime dateTime,
      JsonGenerator serializer,
      SerializerProvider serializerProvider
  ) throws IOException {
    try {
      var result = dateTime.format(ofPattern(dateFormat));
      serializer.writeString(result);
    } catch (DateTimeParseException ex) {
      throw new RuntimeException(format("Could not serialize date [%s]", dateTime.toString()), ex);
    }
  }
}

