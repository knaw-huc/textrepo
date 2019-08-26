package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultFile {

  private Version version;
  private String filename;

  public ResultFile(String filename, Version version) {
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
