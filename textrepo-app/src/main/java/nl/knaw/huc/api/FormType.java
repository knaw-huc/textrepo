package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;

import javax.validation.constraints.NotBlank;

public class FormType {

  @ApiParam(required = true, example = "text/plain")
  @NotBlank(message = "is mandatory")
  @ApiModelProperty(example = "plaintext", required = true)
  private final String name;

  @ApiParam(required = true, example = "text/plain")
  @NotBlank(message = "is mandatory")
  @ApiModelProperty(example = "text/plain", required = true)
  private final String mimetype;

  @JsonCreator
  public FormType(
      @ApiParam(required = true, example = "plaintext")
      @JsonProperty("name")
          String name,
      @ApiParam(required = true, example = "text/plain")
      @JsonProperty("mimetype")
          String mimetype
  ) {
    this.name = name;
    this.mimetype = mimetype;
  }

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
