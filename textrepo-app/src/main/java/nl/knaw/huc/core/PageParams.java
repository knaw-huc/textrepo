package nl.knaw.huc.core;

import com.google.common.base.MoreObjects;

public class PageParams implements Paginated {

  private int limit;

  private int offset;

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
  public int getLimit() {
    return limit;
  }

  @Override
  public int getOffset() {
    return offset;
  }
}
