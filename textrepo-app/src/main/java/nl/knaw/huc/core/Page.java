package nl.knaw.huc.core;

import java.util.List;

public class Page<T> {

  private List<T> content;
  private int total;
  private PageParams params;

  public Page(List<T> content, int total, PageParams params) {
    this.content = content;
    this.total = total;
    this.params = params;
  }

  public List<T> getContent() {
    return content;
  }

  public int getTotal() {
    return total;
  }

  public PageParams getParams() {
    return params;
  }
}
