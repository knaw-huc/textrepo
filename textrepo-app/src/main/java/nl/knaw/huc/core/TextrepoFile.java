package nl.knaw.huc.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class TextrepoFile {

  private final UUID id;
  private final Short typeId;

  @ConstructorProperties({"id", "type_id"})
  public TextrepoFile(UUID id, Short typeId) {
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

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("id", id)
        .add("typeId", typeId)
        .toString();
  }
}
