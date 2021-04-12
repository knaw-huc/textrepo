package nl.knaw.huc.resources.view;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.helpers.ContentsHelper;

import javax.ws.rs.NotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static java.lang.String.format;

public class ViewBuilderFactory {
  private static final Map<String, BiFunction<Contents, ContentsHelper, Object>> registry = new HashMap<>();

  public void register(String viewName, BiFunction<Contents, ContentsHelper, Object> viewBuilder) {
    registry.put(viewName, viewBuilder);
  }

  public BiFunction<Contents, ContentsHelper, Object> createView(String view) {
    final var builder = registry.get(view);

    if (builder == null) {
      throw new NotFoundException(format("Unknown view: %s", view));
    }

    return builder;
  }
}
