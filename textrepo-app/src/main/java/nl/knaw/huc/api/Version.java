package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;
import java.time.LocalDateTime;
import java.util.UUID;

public class Version {

  private UUID documentUuid;
  private LocalDateTime date;
  private String contentsSha;

  @ConstructorProperties({"document_uuid", "date", "file_sha"})
  public Version(UUID documentUuid, LocalDateTime date, String contentsSha) {
    this.documentUuid = documentUuid;
    this.date = date;
    this.contentsSha = contentsSha;
  }

  @JsonProperty
  public UUID getDocumentUuid() {
    return documentUuid;
  }

  @JsonProperty
  public void setDocumentUuid(UUID documentUuid) {
    this.documentUuid = documentUuid;
  }

  @JsonProperty
  public LocalDateTime getDate() {
    return date;
  }

  @JsonProperty
  public void setDate(LocalDateTime date) {
    this.date = date;
  }

  @JsonProperty
  public String getContentsSha() {
    return contentsSha;
  }

  @JsonProperty
  public void setContentsSha(String contentsSha) {
    this.contentsSha = contentsSha;
  }
}
