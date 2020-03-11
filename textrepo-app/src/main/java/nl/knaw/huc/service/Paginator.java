package nl.knaw.huc.service;

import nl.knaw.huc.PaginationConfiguration;
import nl.knaw.huc.api.FormPageParams;
import nl.knaw.huc.api.ResultPage;
import nl.knaw.huc.api.ResultPageParams;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;

import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class Paginator {

  private final PaginationConfiguration config;

  public Paginator(PaginationConfiguration config) {
    this.config = config;
  }

  /**
   * Use form params, or set defaults
   */
  public PageParams withDefaults(FormPageParams form) {
    var limit = form.getLimit() == null ?
        config.defaultLimit :
        form.getLimit();
    var offset = form.getOffset() == null ?
        config.defaultOffset :
        form.getOffset();
    return new PageParams(limit, offset);
  }

  public static <T, U> ResultPage<U> mapResult(Page<T> page, Function<T, U> mapper) {
    var resultContent = page.getItems()
        .stream()
        .map(mapper)
        .collect(toList());
    var resultParams = new ResultPageParams(
        page.getParams().getLimit(),
        page.getParams().getOffset()
    );
    return new ResultPage<>(
        resultContent,
        page.getTotal(),
        resultParams
    );
  }

}
