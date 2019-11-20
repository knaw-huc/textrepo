package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultContents {

  private Version version;
  private String filename;

  public ResultContents(String filename, Version version) {
    this.version = version;
    this.filename = filename;
  }

  @JsonProperty
  public Version getVersion() {
    return version;
  }

  @JsonProperty
  public String getFilename() {
    return filename;
  }
}
