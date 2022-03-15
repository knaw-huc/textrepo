package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.UUID;
import nl.knaw.huc.core.Document;

public class ResultDocument {
  private final UUID id;
  private final String externalId;
  private final LocalDateTime createdAt;

  public ResultDocument(Document document) {
    this.id = document.getId();
    this.externalId = document.getExternalId();
    this.createdAt = document.getCreatedAt();
  }

  @JsonProperty
  @ApiModelProperty(
      value = "internal document identifier as used, e.g., in rest URIs",
      example = "34739357-eb75-449b-b2df-d3f6289470d6")
  public UUID getId() {
    return id;
  }

  @JsonProperty
  @ApiModelProperty(position = 1,
      value = "external document identifier",
      example = "document_1234")
  public String getExternalId() {
    return externalId;
  }

  @JsonProperty
  @ApiModelProperty(position = 2,
      value = "date and time of document creation",
      example = "2021-04-16T09:03:03")
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

}
