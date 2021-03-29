package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;

public class FormVersion {

  @NotBlank(message = "is mandatory")
  @ApiModelProperty(example = "document_1234")
  private final String externalId;

  @JsonCreator
  public FormVersion(
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
