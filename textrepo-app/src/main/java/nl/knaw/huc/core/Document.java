package nl.knaw.huc.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class Document {

  private UUID id;
  private String externalId;

  @ConstructorProperties({"id", "external_id"})
  public Document(UUID id, String externalId) {
    this.id = id;
    this.externalId = externalId;
  }

  @JsonProperty
  public UUID getId() {
    return id;
  }

  @JsonProperty
  public void setId(UUID id) {
    this.id = id;
  }

  @JsonProperty
  public String getExternalId() {
    return externalId;
  }

  @JsonProperty
  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

}
