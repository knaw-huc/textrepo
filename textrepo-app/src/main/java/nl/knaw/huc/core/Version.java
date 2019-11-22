package nl.knaw.huc.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A file can contain different versions
 */
public class Version {

  private UUID fileUuid;
  private LocalDateTime date;
  private String contentsSha;

  @ConstructorProperties({"file_uuid", "date", "contents_sha"})
  public Version(UUID fileUuid, LocalDateTime date, String contentsSha) {
    this.fileUuid = fileUuid;
    this.date = date;
    this.contentsSha = contentsSha;
  }

  @JsonProperty
  public UUID getFileUuid() {
    return fileUuid;
  }

  @JsonProperty
  public void setFileUuid(UUID fileUuid) {
    this.fileUuid = fileUuid;
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
