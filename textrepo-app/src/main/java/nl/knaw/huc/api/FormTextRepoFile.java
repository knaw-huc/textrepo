package nl.knaw.huc.api;

import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class FormTextRepoFile {

  @ApiModelProperty(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
  @NotNull(message = "is mandatory")
  public UUID docId;

  @ApiModelProperty(required = true, example = "1")
  @NotNull(message = "is mandatory")
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
