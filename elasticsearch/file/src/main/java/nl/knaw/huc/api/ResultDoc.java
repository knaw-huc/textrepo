package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public class ResultDoc {
  private UUID id;
  private String externalId;
  private List<ResultMetadataEntry> metadata;

  @JsonProperty
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  @JsonProperty
  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  @JsonProperty
  public List<ResultMetadataEntry> getMetadata() {
    return metadata;
  }

  public void setMetadata(List<ResultMetadataEntry> metadata) {
    this.metadata = metadata;
  }
}
