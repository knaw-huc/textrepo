package nl.knaw.huc.service.datetime;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class LocalDateTimeParamConverterProvider implements ParamConverterProvider {

  private final String dateTimePattern;

  public LocalDateTimeParamConverterProvider(String dateTimePattern) {
    this.dateTimePattern = dateTimePattern;
  }

  @Override
  public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
    if (rawType.equals(LocalDateTime.class)) {
      return (ParamConverter<T>) new LocalDateTimeParamConverter(dateTimePattern);
    }
    return null;
  }
}
