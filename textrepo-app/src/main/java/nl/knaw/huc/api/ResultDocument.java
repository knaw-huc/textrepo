package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.core.Document;

import java.util.UUID;

public class ResultDocument {
  private UUID id;
  private String externalId;

  public ResultDocument(Document document) {
    this.id = document.getId();
    this.externalId = document.getExternalId();
  }

  @JsonProperty
  public UUID getId() {
    return id;
  }

  @JsonProperty
  public String getExternalId() {
    return externalId;
  }

}