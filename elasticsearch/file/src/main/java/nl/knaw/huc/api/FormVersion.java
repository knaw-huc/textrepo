package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public class FormVersion {

  private final UUID id;
  private final String sha;
  private final LocalDateTime createdAt;

  @JsonCreator
  public FormVersion(
      @JsonProperty("id") UUID id,
      @JsonProperty("createdAt") LocalDateTime createdAt,
      @JsonProperty("contentsSha") String sha
  ) {
    this.id = id;
    this.createdAt = createdAt;
    this.sha = sha;
  }


  public UUID getId() {
    return id;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public String getSha() {
    return sha;
  }
}
