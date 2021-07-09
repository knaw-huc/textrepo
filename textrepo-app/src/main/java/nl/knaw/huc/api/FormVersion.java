package nl.knaw.huc.api;

import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiParam;

import javax.validation.constraints.NotBlank;

public class FormVersion {

  @NotBlank(message = "is mandatory")
  @ApiParam(required = true, example = "document_1234")
  public String externalId;

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("externalId", externalId)
        .toString();
  }
}
