package nl.knaw.huc.service.datetime;

import static org.eclipse.jetty.util.StringUtil.isBlank;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ext.ParamConverter;

/**
 * Convert Jersey datetime @QueryParams to LocalDateTime
 *
 * <p>Datetime pattern is configured in config.yml
 */
public class LocalDateTimeParamConverter implements ParamConverter<LocalDateTime> {

  private final DateTimeFormatter formatter;
  private final String pattern;

  public LocalDateTimeParamConverter(String pattern) {
    this.formatter = DateTimeFormatter.ofPattern(pattern);
    this.pattern = pattern;
  }

  @Override
  public LocalDateTime fromString(String dateTime) {
    if (isBlank(dateTime)) {
      return null;
    }
    try {
      return LocalDateTime.parse(dateTime, formatter);
    } catch (DateTimeParseException ex) {
      throw new BadRequestException("Date format should be: " + pattern);
    }
  }

  @Override
  public String toString(LocalDateTime dateTime) {
    return formatter.format(dateTime);
  }
}
