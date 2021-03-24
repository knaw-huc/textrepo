package nl.knaw.huc.resources.view;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.helpers.ContentsHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class ViewBuilderFactory {
  private static final Map<String, BiFunction<Contents, ContentsHelper, Object>> registry = new HashMap<>();

  public void register(String viewName, BiFunction<Contents, ContentsHelper, Object> viewBuilder) {
    registry.put(viewName, viewBuilder);
  }

  public Optional<BiFunction<Contents, ContentsHelper, Object>> createView(String view) {
    return Optional.ofNullable(registry.get(view));
  }
}
