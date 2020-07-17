package nl.knaw.huc.service.index;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldsConfiguration {

  @JsonProperty
  public String url;

  @JsonProperty
  public FieldsType type;

  public static FieldsConfiguration build(String type, String url) {
    var result = new FieldsConfiguration();
    result.type = FieldsType.fromString(type);
    result.url = url;
    return result;
  }
}
