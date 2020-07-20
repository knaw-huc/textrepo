package nl.knaw.huc.core;

import com.google.common.base.MoreObjects;

import java.beans.ConstructorProperties;
import java.time.LocalDateTime;
import java.util.UUID;

public class Version {
  private final UUID id;
  private final UUID fileId;
  private final String contentsSha;
  private final LocalDateTime createdAt;

  @ConstructorProperties({"id", "file_id", "contents_sha", "created_at"})
  public Version(UUID id, UUID fileId, String contentsSha, LocalDateTime createdAt) {
    this.id = id;
    this.fileId = fileId;
    this.contentsSha = contentsSha;
    this.createdAt = createdAt;
  }

  public Version(UUID id, UUID fileId, String contentsSha) {
    this(id, fileId, contentsSha, null);
  }

  public UUID getId() {
    return id;
  }

  public UUID getFileId() {
    return fileId;
  }

  public String getContentsSha() {
    return contentsSha;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("id", id)
        .add("fileId", fileId)
        .add("contentsSha", contentsSha)
        .add("createdAt", createdAt)
        .toString();
  }
}
