package nl.knaw.huc.core;

import com.google.common.base.MoreObjects;

import java.beans.ConstructorProperties;
import java.time.LocalDateTime;
import java.util.UUID;

public class Version {
  private UUID id;
  private UUID fileId;
  private LocalDateTime createdAt;
  private String contentsSha;

  @ConstructorProperties({"id", "file_id", "contents_sha", "created_at"})
  public Version(UUID id, UUID fileId, String contentsSha, LocalDateTime createdAt) {
    this(id, fileId, contentsSha);
    this.createdAt = createdAt;
  }

  public Version(UUID id, UUID fileId, String contentsSha) {
    this.id = id;
    this.fileId = fileId;
    this.contentsSha = contentsSha;
  }

  public UUID getId() {
    return id;
  }

  public UUID getFileId() {
    return fileId;
  }

  public void setFileId(UUID fileId) {
    this.fileId = fileId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public String getContentsSha() {
    return contentsSha;
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("fileId", fileId)
        .add("createdAt", createdAt)
        .add("contentsSha", contentsSha)
        .toString();
  }
}
