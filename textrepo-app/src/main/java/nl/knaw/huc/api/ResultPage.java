package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.core.PageParams;

import java.util.List;

public class ResultPage<T> {

  private List<T> content;
  private int total;
  private ResultPageParams params;

  public ResultPage(List<T> content, int total, ResultPageParams params) {
    this.content = content;
    this.total = total;
    this.params = params;
  }

  @JsonProperty
  public List<T> getContent() {
    return content;
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
