package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ResultFields {

  private ResultFile file;
  private ResultDoc doc;
  private List<ResultVersion> versions;

  public ResultFields() {
  }

  @JsonProperty
  public ResultDoc getDoc() {
    return doc;
  }

  public void setDoc(ResultDoc doc) {
    this.doc = doc;
  }

  @JsonProperty
  public ResultFile getFile() {
    return file;
  }

  public void setFile(ResultFile file) {
    this.file = file;
  }

  @JsonProperty
  public List<ResultVersion> getVersions() {
    return versions;
  }

  public void setVersions(List<ResultVersion> versions) {
    this.versions = versions;
  }

}
