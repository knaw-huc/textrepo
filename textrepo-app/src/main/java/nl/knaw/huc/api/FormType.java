package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;

public class FormType {

  @NotBlank(message = "is mandatory")
  @ApiModelProperty(example = "plaintext")
  private final String name;

  @NotBlank(message = "is mandatory")
  @ApiModelProperty(example = "text/plain")
  private final String mimetype;

  @JsonCreator
  public FormType(
      @JsonProperty("name") String name,
      @JsonProperty("mimetype") String mimetype
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
