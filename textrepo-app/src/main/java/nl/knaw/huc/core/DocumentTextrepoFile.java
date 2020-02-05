package nl.knaw.huc.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class DocumentTextrepoFile {

  private UUID id;
  private Short typeId;

  @ConstructorProperties({"id", "type_id"})
  public DocumentTextrepoFile(UUID id, Short typeId) {
    this.id = id;
    this.typeId = typeId;
  }

  @JsonProperty
  public UUID getId() {
    return id;
  }

  @JsonProperty
  public Short getTypeId() {
    return typeId;
  }

  @JsonProperty
  public void setId(UUID id) {
    this.id = id;
  }

  @JsonProperty
  public void setTypeId(Short typeId) {
    this.typeId = typeId;
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("id", id)
        .add("typeId", typeId)
        .toString();
  }
}
