package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.core.Version;

import java.time.LocalDateTime;
import java.util.UUID;

public class ResultVersion {
  private UUID fileId;
  private LocalDateTime createdAt;
  private String contentsSha;

  public ResultVersion(Version version) {
    this.fileId = version.getFileId();
    this.createdAt = version.getCreatedAt();
    this.contentsSha = version.getContentsSha();
  }

  @JsonProperty
  public UUID getFileId() {
    return fileId;
  }

  @JsonProperty
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty
  public String getContentsSha() {
    return contentsSha;
  }

}
