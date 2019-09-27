package nl.knaw.huc.textrepo;

import static java.lang.System.getenv;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class Config {

  public static final String HTTP_ES_HOST = "http://" + requireNonBlank(getenv("ES_HOST"));
  public static final String HTTP_APP_HOST = "http://" + requireNonBlank(getenv("APP_HOST"));
  public static final String POSTGRES_PASSWORD = requireNonBlank(getenv("POSTGRES_PASSWORD"));
  public static final String POSTGRES_DB = requireNonBlank(getenv("POSTGRES_DB"));
  public static final String POSTGRES_USER = requireNonBlank(getenv("POSTGRES_USER"));
  public static final String POSTGRES_HOST = requireNonBlank(getenv("POSTGRES_HOST"));
  public static final String DOCUMENT_INDEX = requireNonBlank(getenv("DOCUMENT_INDEX"));
  public static final String CUSTOM_INDEX = requireNonBlank(getenv("CUSTOM_INDEX"));

  private static String requireNonBlank(String field) {
    if (isBlank(field)) {
      throw new RuntimeException("Environment variable is not set");
    }
    return field;
  }

}
