package nl.knaw.huc.api;

import com.google.common.base.MoreObjects;
import nl.knaw.huc.core.Paginated;

import javax.ws.rs.QueryParam;

public class FormPageParams implements Paginated {

  @QueryParam("limit")
  private Integer limit;

  @QueryParam("offset")
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
