package nl.knaw.huc.core;

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

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

}
