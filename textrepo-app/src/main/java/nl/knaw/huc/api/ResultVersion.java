package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.core.Version;

import java.time.LocalDateTime;
import java.util.UUID;

public class ResultVersion {

  private UUID fileUuid;
  private LocalDateTime date;
  private String contentsSha;

  public ResultVersion(Version version) {
    this.fileUuid = version.getFileUuid();
    this.date = version.getDate();
    this.contentsSha = version.getContentsSha();
  }

  @JsonProperty
  public UUID getFileUuid() {
    return fileUuid;
  }

  @JsonProperty
  public LocalDateTime getDate() {
    return date;
  }

  @JsonProperty
  public String getContentsSha() {
    return contentsSha;
  }

}
