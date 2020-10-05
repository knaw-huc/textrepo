package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.UUID;

public class ResultFile {
  private UUID id;
  private ResultType type;
  private Map<String, String> metadata;

  @JsonProperty
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  @JsonProperty
  public ResultType getType() {
    return type;
  }

  public void setType(ResultType type) {
    this.type = type;
  }

  @JsonProperty
  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }
}
