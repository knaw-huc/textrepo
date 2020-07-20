package nl.knaw.huc.core;

import com.google.common.base.MoreObjects;

public class PageParams implements Paginated {

  private final int limit;

  private final int offset;

  public PageParams(int limit, int offset) {
    this.limit = limit;
    this.offset = offset;
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("limit", limit)
        .add("offset", offset)
        .toString();
  }

  @Override
  public Integer getLimit() {
    return limit;
  }

  @Override
  public Integer getOffset() {
    return offset;
  }
}
