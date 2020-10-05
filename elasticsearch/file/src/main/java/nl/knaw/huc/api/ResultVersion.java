package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public class ResultVersion {
  private UUID id;
  private LocalDateTime createdAt;
  private String sha;
  private Boolean contentsChanged;

  @JsonProperty
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  @JsonProperty
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @JsonProperty
  public String getSha() {
    return sha;
  }

  public void setSha(String sha) {
    this.sha = sha;
  }

  @JsonProperty
  public Boolean getContentsChanged() {
    return contentsChanged;
  }

  public void setContentsChanged(Boolean contentsChanged) {
    this.contentsChanged = contentsChanged;
  }
}
