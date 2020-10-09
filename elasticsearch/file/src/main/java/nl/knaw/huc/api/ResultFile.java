package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public class ResultFile {
  private UUID id;
  private ResultType type;
  private List<ResultMetadataEntry> metadata;

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
  public List<ResultMetadataEntry> getMetadata() {
    return metadata;
  }

  public void setMetadata(List<ResultMetadataEntry> metadata) {
    this.metadata = metadata;
  }
}
