package nl.knaw.huc.service;

import nl.knaw.huc.PaginationConfiguration;
import nl.knaw.huc.api.FormPageParams;
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

  public static <T, U> Page<U> mapPage(Page<T> page, Function<T, U> mapper) {
    var converted = page.getContent()
        .stream()
        .map(mapper)
        .collect(toList());
    return new Page<>(converted, page.getTotal(), page.getParams());
  }

}
