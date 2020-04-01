package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ResultPage<T> {

  private List<T> items;
  private int total;
  private ResultPageParams page;

  public ResultPage(List<T> items, int total, ResultPageParams page) {
    this.items = items;
    this.total = total;
    this.page = page;
  }

  @JsonProperty
  public List<T> getItems() {
    return items;
  }

  @JsonProperty
  public int getTotal() {
    return total;
  }

  @JsonProperty
  public ResultPageParams getPage() {
    return page;
  }
}