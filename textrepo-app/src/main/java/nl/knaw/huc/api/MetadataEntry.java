package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class MetadataEntry {

  private UUID documentUuid;
  private String key;
  private String value;

  @ConstructorProperties({"document_uuid", "key", "value"})
  public MetadataEntry(UUID documentUuid, String key, String value) {
    this.documentUuid = documentUuid;
    this.key = key;
    this.value = value;
  }

  @JsonProperty
  public UUID getDocumentUuid() {
    return documentUuid;
  }

  @JsonProperty
  public void setDocumentUuid(UUID documentUuid) {
    this.documentUuid = documentUuid;
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
