package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Fields {

  private UUID id;
  private String type;
  private String mimetype;
  private UUID docId;
  private String docExternalId;
  private Map<String, String> docMetadata;
  private Map<String, String> fileMetadata;
  private List<Version> versions;

  public Fields(UUID id) {
    this.id = id;
  }

  @JsonProperty
  public UUID getId() {
    return id;
  }

  @JsonProperty
  public String getType() {
    return type;
  }

  @JsonProperty
  public String getMimetype() {
    return mimetype;
  }

  @JsonProperty
  public UUID getDocId() {
    return docId;
  }

  @JsonProperty
  public String getDocExternalId() {
    return docExternalId;
  }

  @JsonProperty
  public Map<String, String> getDocMetadata() {
    return docMetadata;
  }

  @JsonProperty
  public Map<String, String> getFileMetadata() {
    return fileMetadata;
  }

  @JsonProperty
  public List<Version> getVersions() {
    return versions;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setMimetype(String mimetype) {
    this.mimetype = mimetype;
  }

  public void setDocId(UUID docId) {
    this.docId = docId;
  }

  public void setDocExternalId(String docExternalId) {
    this.docExternalId = docExternalId;
  }

  public void setDocMetadata(Map<String, String> docMetadata) {
    this.docMetadata = docMetadata;
  }

  public void setFileMetadata(Map<String, String> fileMetadata) {
    this.fileMetadata = fileMetadata;
  }

  public void setVersions(List<Version> versions) {
    this.versions = versions;
  }
}
