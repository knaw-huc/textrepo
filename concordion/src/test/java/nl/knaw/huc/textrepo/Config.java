package nl.knaw.huc.textrepo;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class Config {
  public static final String HTTP_ES_HOST = "http://" + requireNonBlank("ES_HOST");
  public static final String HTTP_APP_HOST = "http://" + requireNonBlank("APP_HOST");
  public static final String HTTP_APP_HOST_ADMIN = "http://" + requireNonBlank("APP_HOST_ADMIN");
  public static final String POSTGRES_PASSWORD = requireNonBlank("POSTGRES_PASSWORD");
  public static final String POSTGRES_DB = requireNonBlank("POSTGRES_DB");
  public static final String POSTGRES_USER = requireNonBlank("POSTGRES_USER");
  public static final String POSTGRES_HOST = requireNonBlank("POSTGRES_HOST");
  public static final String FULL_TEXT_INDEX = requireNonBlank("FULL_TEXT_INDEX");
  public static final String CUSTOM_INDEX = requireNonBlank("CUSTOM_INDEX");
  public static final String AUTOCOMPLETE_INDEX = requireNonBlank("AUTOCOMPLETE_INDEX");

  public static final String HOST = HTTP_APP_HOST;
  public static final String FILES_URL = HOST + "/files";
  public static final String TYPES_URL = HOST + "/rest/types";

  public static final List<String> INDICES = newArrayList(
      FULL_TEXT_INDEX,
      CUSTOM_INDEX,
      AUTOCOMPLETE_INDEX
  );
  public static final String TEXT_TYPE = "text";
  public static final String TEXT_MIMETYPE = "text/plain";

  public static final String FOO_TYPE = "foo";
  public static final String FOO_MIMETYPE = "foo/bar";

  private static String requireNonBlank(String field) {
    final var value = getenv(field);
    if (isBlank(value)) {
      throw new RuntimeException(format("Env var [%s] is not set", field));
    }
    return value;
  }

}
