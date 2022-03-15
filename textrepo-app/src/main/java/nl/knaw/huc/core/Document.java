package nl.knaw.huc.core;

import com.google.common.base.MoreObjects;
import java.beans.ConstructorProperties;
import java.time.LocalDateTime;
import java.util.UUID;

public class Document {

  private UUID id;
  private String externalId;
  private LocalDateTime createdAt;

  @ConstructorProperties({"id", "external_id", "created_at"})
  public Document(UUID id, String externalId, LocalDateTime createdAt) {
    this(id, externalId);
    this.createdAt = createdAt;
  }

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

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("id", id)
        .add("externalId", externalId)
        .add("createdAt", createdAt)
        .toString();
  }
}
