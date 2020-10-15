package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class ResultMetadataEntry {
  private String key;
  private String value;

  public ResultMetadataEntry(String key, String value) {
    this.key = key;
    this.value = value;
  }

  @JsonProperty
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  @JsonProperty
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
