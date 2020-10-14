package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public class ResultContentsLastModified {
  private LocalDateTime dateTime;
  private String contentsSha;
  private UUID versionId;

  @JsonProperty
  public LocalDateTime getDateTime() {
    return dateTime;
  }

  public void setDateTime(LocalDateTime dateTime) {
    this.dateTime = dateTime;
  }

  @JsonProperty
  public String getContentsSha() {
    return contentsSha;
  }

  public void setContentsSha(String contentsSha) {
    this.contentsSha = contentsSha;
  }

  @JsonProperty
  public UUID getVersionId() {
    return versionId;
  }

  public void setVersionId(UUID versionId) {
    this.versionId = versionId;
  }
}
