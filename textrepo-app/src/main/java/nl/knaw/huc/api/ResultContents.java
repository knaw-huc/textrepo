package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultContents {

  private final ResultVersion version;
  private final String filename;

  public ResultContents(String filename, ResultVersion version) {
    this.version = version;
    this.filename = filename;
  }

  @JsonProperty
  public ResultVersion getVersion() {
    return version;
  }

  @JsonProperty
  public String getFilename() {
    return filename;
  }
}
