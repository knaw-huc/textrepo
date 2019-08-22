package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;

public class KeyValue {
  @JsonProperty
  public final String key;
  @JsonProperty
  public final String value;

  @ConstructorProperties({"key", "value"})
  public KeyValue(String key, String value) {
    this.key = key;
    this.value = value;
  }
}
