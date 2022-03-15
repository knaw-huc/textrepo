package nl.knaw.huc.service.datetime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

public class LocalDateTimeParamConverterProvider implements ParamConverterProvider {

  private final String dateTimePattern;

  public LocalDateTimeParamConverterProvider(String dateTimePattern) {
    this.dateTimePattern = dateTimePattern;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType,
                                            Annotation[] annotations) {
    if (rawType.equals(LocalDateTime.class)) {
      return (ParamConverter<T>) new LocalDateTimeParamConverter(dateTimePattern);
    }
    return null;
  }
}
