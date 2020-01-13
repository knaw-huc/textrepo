package nl.knaw.huc.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.beans.ConstructorProperties;

public class Type {

  private final String name;
  private final String mimetype;

  @ConstructorProperties({"name", "mimetype"})
  public Type(String name, String mimetype) {
    this.name = name;
    this.mimetype = mimetype;
  }

  @JsonProperty
  public String getName() {
    return name;
  }

  @JsonProperty
  public String getMimetype() {
    return mimetype;
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("name", name)
        .add("mimetype", mimetype)
        .toString();
  }
}
