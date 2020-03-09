package nl.knaw.huc.api;

import com.google.common.base.MoreObjects;
import nl.knaw.huc.core.Paginated;

import javax.ws.rs.QueryParam;

public class FormPageParams implements Paginated {

  private final int defaultLimit = 0;

  @QueryParam("limit")
  private int limit;

  @QueryParam("offset")
  private int offset;

  @Override
  public int getLimit() {
    return limit;
  }

  @Override
  public int getOffset() {
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
