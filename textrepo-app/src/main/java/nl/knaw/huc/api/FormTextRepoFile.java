package nl.knaw.huc.api;

import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class FormTextRepoFile {

  @NotNull(message = "is mandatory")
  @ApiParam(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
  public UUID docId;

  @NotNull(message = "is mandatory")
  @ApiModelProperty(required = true, example = "1")
  public Short typeId;

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("docId", docId)
        .add("typeId", typeId)
        .toString();
  }
}
