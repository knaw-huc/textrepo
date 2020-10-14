package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultType {
  private Integer id;
  private String name;
  private String mimetype;

  @JsonProperty
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @JsonProperty
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @JsonProperty
  public String getMimetype() {
    return mimetype;
  }

  public void setMimetype(String mimetype) {
    this.mimetype = mimetype;
  }
}
