package nl.knaw.huc.core;

import com.google.common.base.MoreObjects;
import java.beans.ConstructorProperties;

public class Type {

  private short id;
  private final String name;
  private final String mimetype;

  public Type(String name, String mimetype) {
    this.name = name;
    this.mimetype = mimetype;
  }

  @ConstructorProperties({"id", "name", "mimetype"})
  public Type(short id, String name, String mimetype) {
    this.id = id;
    this.name = name;
    this.mimetype = mimetype;
  }

  public short getId() {
    return id;
  }

  public void setId(short id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

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
