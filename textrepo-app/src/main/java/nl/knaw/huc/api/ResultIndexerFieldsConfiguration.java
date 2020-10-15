package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.service.index.FieldsConfiguration;

public class ResultIndexerFieldsConfiguration {
  private final String type;
  private final String url;

  public ResultIndexerFieldsConfiguration(FieldsConfiguration fields) {
    this.type = fields.type.getName();
    this.url = fields.url;
  }

  @JsonProperty
  public String getType() {
    return type;
  }

  @JsonProperty
  public String getUrl() {
    return url;
  }
}
