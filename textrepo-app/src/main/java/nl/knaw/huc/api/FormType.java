package nl.knaw.huc.api;

import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;

import javax.validation.constraints.NotBlank;

public class FormType {

  @ApiModelProperty(required = true, example = "plaintext")
  @NotBlank(message = "is mandatory")
  public String name;

  @ApiModelProperty(required = true, example = "text/plain")
  @NotBlank(message = "is mandatory")
  public String mimetype;

  public String getName() {
    return name;
  }

  public String getMimetype() {
    return mimetype;
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("name", name)
        .add("mimetype", mimetype)
        .toString();
  }
}
