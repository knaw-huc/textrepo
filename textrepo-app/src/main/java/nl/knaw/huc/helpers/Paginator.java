package nl.knaw.huc.helpers;

import static java.util.stream.Collectors.toList;

import java.util.function.Function;
import nl.knaw.huc.api.FormPageParams;
import nl.knaw.huc.api.ResultPage;
import nl.knaw.huc.api.ResultPageParams;
import nl.knaw.huc.config.PaginationConfiguration;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;

public class Paginator {

  private final PaginationConfiguration config;

  public Paginator(PaginationConfiguration config) {
    this.config = config;
  }

  public static <T, U> ResultPage<U> toResult(Page<T> page, Function<T, U> mapper) {
    var resultContent = page.getItems().stream().map(mapper).collect(toList());
    var resultParams =
        new ResultPageParams(page.getParams().getLimit(), page.getParams().getOffset());
    return new ResultPage<>(resultContent, page.getTotal(), resultParams);
  }

  /**
   * Use form params, or set defaults.
   */
  public PageParams fromForm(FormPageParams form) {
    var limit = form.getLimit() == null ? config.defaultLimit : form.getLimit();
    var offset = form.getOffset() == null ? config.defaultOffset : form.getOffset();
    return new PageParams(limit, offset);
  }

}
