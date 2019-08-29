package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;

public class MetadataEntry {
  private String key;
  private String value;

  @ConstructorProperties({"key", "value"})
  public MetadataEntry(String key, String value) {
    this.key = key;
    this.value = value;
  }

  @JsonProperty
  public String getKey() {
    return key;
  }

  @JsonProperty
  public void setKey(String key) {
    this.key = key;
  }

  @JsonProperty
  public String getValue() {
    return value;
  }

  @JsonProperty
  public void setValue(String value) {
    this.value = value;
  }
}
