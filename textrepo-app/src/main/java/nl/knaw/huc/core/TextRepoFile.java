package nl.knaw.huc.core;

import com.google.common.base.MoreObjects;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class TextRepoFile {

  private UUID id;
  private Short typeId;

  @ConstructorProperties({"id", "type_id"})
  public TextRepoFile(UUID id, Short typeId) {
    this.id = id;
    this.typeId = typeId;
  }

  public UUID getId() {
    return id;
  }

  public Short getTypeId() {
    return typeId;
  }

  public void setId(UUID id) {
    this.id = id;
  }

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
