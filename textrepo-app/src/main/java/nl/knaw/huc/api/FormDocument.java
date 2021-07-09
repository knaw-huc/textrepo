package nl.knaw.huc.api;

import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;

import javax.validation.constraints.NotBlank;

public class FormDocument {

  @NotBlank(message = "is mandatory")
  @ApiModelProperty(example = "document_1234", required = true)
  public String externalId;

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
