package nl.knaw.huc.service.index;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldsConfiguration {

  @JsonProperty
  public String url;

  /**
   * Options:
   * - urlencoded: "application/x-www-form-urlencoded"
   * - multipart: "multipart/form-data"
   */
  @JsonProperty
  public String type;

  public static FieldsConfiguration build(String type, String url) {
    var result = new FieldsConfiguration();
    result.type = type;
    result.url = url;
    return result;
  }
}
