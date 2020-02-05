package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.core.Type;

public class ResultType {

  private short id;
  private final String name;
  private final String mimetype;

  public ResultType(Type type) {
    this.id = type.getId();
    this.name = type.getName();
    this.mimetype = type.getMimetype();
  }

  @JsonProperty
  public short getId() {
    return id;
  }

  @JsonProperty
  public String getName() {
    return name;
  }

  @JsonProperty
  public String getMimetype() {
    return mimetype;
  }

}
