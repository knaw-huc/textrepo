package nl.knaw.huc.textrepo;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class Config {

  public static final String HTTP_ES_HOST = "http://" + requireNonBlank(getenv("ES_HOST"));
  public static final String HTTP_APP_HOST = "http://" + requireNonBlank(getenv("APP_HOST"));
  public static final String POSTGRES_PASSWORD = requireNonBlank(getenv("POSTGRES_PASSWORD"));
  public static final String POSTGRES_DB = requireNonBlank(getenv("POSTGRES_DB"));
  public static final String POSTGRES_USER = requireNonBlank(getenv("POSTGRES_USER"));
  public static final String POSTGRES_HOST = requireNonBlank(getenv("POSTGRES_HOST"));
  public static final String FILE_INDEX = requireNonBlank(getenv("FILE_INDEX"));
  public static final String CUSTOM_INDEX = requireNonBlank(getenv("CUSTOM_INDEX"));
  public static final String AUTOCOMPLETE_INDEX = requireNonBlank(getenv("AUTOCOMPLETE_INDEX"));

  public static final String HOST = HTTP_APP_HOST;
  public static final String FILES_URL = HOST + "/files";
  public static final String TYPES_URL = HOST + "/types";

  public static final List<String> INDICES = newArrayList(
      FILE_INDEX,
      CUSTOM_INDEX,
      AUTOCOMPLETE_INDEX
  );
  public static final String FILE_TYPE = "text";

  private static String requireNonBlank(String field) {
    if (isBlank(field)) {
      throw new RuntimeException(format("Env var [%s] is not set", field));
    }
    return field;
  }

}
