package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import javax.validation.constraints.NotBlank;

public class FormDocument {

  @NotBlank(message = "is mandatory")
  private final String externalId;

  @JsonCreator
  public FormDocument(
      @JsonProperty("externalId") String externalId
  ) {
    this.externalId = externalId;
  }

  public String getExternalId() {
    return externalId;
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("externalId", externalId)
        .toString();
  }
}
