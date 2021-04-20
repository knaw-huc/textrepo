package nl.knaw.huc.api;

import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModelProperty;
import nl.knaw.huc.core.Paginated;

import javax.ws.rs.QueryParam;

public class FormPageParams implements Paginated {

  @QueryParam("limit")
  @ApiModelProperty(value = "how many items to return", example = "20")
  private Integer limit;

  @QueryParam("offset")
  @ApiModelProperty(value = "first item to return", example = "10")
  private Integer offset;

  @Override
  public Integer getLimit() {
    return limit;
  }

  @Override
  public Integer getOffset() {
    return offset;
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("limit", limit)
        .add("offset", offset)
        .toString();
  }

}
