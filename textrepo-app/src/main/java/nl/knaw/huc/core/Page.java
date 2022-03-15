package nl.knaw.huc.core;

import com.google.common.base.MoreObjects;
import java.util.List;

public class Page<T> {

  private final List<T> items;
  private final long total;
  private final PageParams params;

  public Page(List<T> items, long total, PageParams params) {
    this.items = items;
    this.total = total;
    this.params = params;
  }

  public List<T> getItems() {
    return items;
  }

  public long getTotal() {
    return total;
  }

  public PageParams getParams() {
    return params;
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("items", items)
        .add("total", total)
        .add("params", params)
        .toString();
  }
}
