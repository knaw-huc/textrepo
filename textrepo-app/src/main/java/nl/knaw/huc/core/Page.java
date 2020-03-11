package nl.knaw.huc.core;

import java.util.List;

public class Page<T> {

  private List<T> items;
  private int total;
  private PageParams params;

  public Page(List<T> items, int total, PageParams params) {
    this.items = items;
    this.total = total;
    this.params = params;
  }

  public List<T> getItems() {
    return items;
  }

  public int getTotal() {
    return total;
  }

  public PageParams getParams() {
    return params;
  }
}
