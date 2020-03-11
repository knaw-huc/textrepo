package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ResultPage<T> {

  private List<T> items;
  private int total;
  private ResultPageParams params;

  public ResultPage(List<T> items, int total, ResultPageParams params) {
    this.items = items;
    this.total = total;
    this.params = params;
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
  public ResultPageParams getParams() {
    return params;
  }
}
