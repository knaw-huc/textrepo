package nl.knaw.huc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import static com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL;
import static com.jayway.jsonpath.Option.SUPPRESS_EXCEPTIONS;

/**
 * Preconfigured JsonPath ParseContect with:
 * - jackson mapping
 * - jackson json provider
 * - null values instead of PathNotFoundExceptions
 */
public class JsonPathFactory {

  public static ParseContext withJackson(ObjectMapper objectMapper) {
    return JsonPath.using(Configuration
        .builder()
        .options(DEFAULT_PATH_LEAF_TO_NULL)
        .mappingProvider(new JacksonMappingProvider(objectMapper))
        .jsonProvider(new JacksonJsonNodeJsonProvider(objectMapper))
        .build());
  }

  public static ParseContext withJackson() {
    return JsonPathFactory.withJackson(new ObjectMapper());
  }

}
