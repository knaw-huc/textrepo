package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ResultPage<T> {

  private final List<T> items;
  private final long total;
  private final ResultPageParams page;

  public ResultPage(List<T> items, long total, ResultPageParams page) {
    this.items = items;
    this.total = total;
    this.page = page;
  }

  @JsonProperty
  public List<T> getItems() {
    return items;
  }

  @JsonProperty
  public long getTotal() {
    return total;
  }

  @JsonProperty
  public ResultPageParams getPage() {
    return page;
  }
}
