package nl.knaw.huc.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.beans.ConstructorProperties;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A file can contain different versions
 */
public class Version {

  private UUID fileId;
  private LocalDateTime createdAt;
  private String contentsSha;

  @ConstructorProperties({"file_id", "created_at", "contents_sha"})
  public Version(UUID fileId, LocalDateTime createdAt, String contentsSha) {
    this.fileId = fileId;
    this.createdAt = createdAt;
    this.contentsSha = contentsSha;
  }

  @JsonProperty
  public UUID getFileId() {
    return fileId;
  }

  @JsonProperty
  public void setFileId(UUID fileId) {
    this.fileId = fileId;
  }

  @JsonProperty
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @JsonProperty
  public String getContentsSha() {
    return contentsSha;
  }

  @JsonProperty
  public void setContentsSha(String contentsSha) {
    this.contentsSha = contentsSha;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                      .add("fileId", fileId)
                      .add("createdAt", createdAt)
                      .add("contentsSha", contentsSha)
                      .toString();
  }
}
