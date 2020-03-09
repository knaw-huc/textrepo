package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.core.Paginated;

public class ResultPageParams implements Paginated {

  private final int defaultLimit = 0;

  private Integer limit;

  private Integer offset;

  public ResultPageParams(Integer limit, Integer offset) {
    this.limit = limit;
    this.offset = offset;
  }

  @Override
  @JsonProperty
  public Integer getLimit() {
    return limit;
  }

  @Override
  @JsonProperty
  public Integer getOffset() {
    return offset;
  }


}
