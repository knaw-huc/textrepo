package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;
import java.time.LocalDateTime;
import java.util.UUID;

public class Version {

  private UUID documentUuid;
  private LocalDateTime date;
  private String fileSha;

  @ConstructorProperties( {"document_uuid", "date", "file_sha"})
  public Version(
    UUID documentUuid,
    LocalDateTime date,
    String fileSha
  ) {
    this.documentUuid = documentUuid;
    this.date = date;
    this.fileSha = fileSha;
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
  public String getFileSha() {
    return fileSha;
  }

  @JsonProperty
  public void setFileSha(String fileSha) {
    this.fileSha = fileSha;
  }
}
