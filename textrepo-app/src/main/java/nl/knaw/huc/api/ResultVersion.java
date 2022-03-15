package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;
import nl.knaw.huc.core.Version;

public class ResultVersion {

  private final UUID id;
  private final UUID fileId;
  private final LocalDateTime createdAt;
  private final String contentsSha;

  public ResultVersion(Version version) {
    this.id = version.getId();
    this.fileId = version.getFileId();
    this.createdAt = version.getCreatedAt();
    this.contentsSha = version.getContentsSha();
  }

  @JsonProperty
  public UUID getId() {
    return id;
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
