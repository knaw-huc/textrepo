package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;

public class ResultFields {

  private ResultFile file;
  private ResultDoc doc;
  private List<ResultVersion> versions;
  private ResultContentsLastModified contentsLastModified;
  private LocalDateTime indexedOn;

  public ResultFields() {
    indexedOn = now();
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

  @JsonProperty
  public ResultContentsLastModified getContentsLastModified() {
    return contentsLastModified;
  }

  public void setContentsLastModified(ResultContentsLastModified contentsLastModified) {
    this.contentsLastModified = contentsLastModified;
  }

  @JsonProperty
  public LocalDateTime getIndexedOn() {
    return indexedOn;
  }
}
