package nl.knaw.huc.resources.view;

import nl.knaw.huc.core.Contents;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ViewBuilderFactory {
  private static final Map<String, Function<Contents, Object>> registry = new HashMap<>();

  public void register(String viewName, Function<Contents, Object> viewBuilder) {
    registry.put(viewName, viewBuilder);
  }

  public Optional<Function<Contents, Object>> createView(String view) {
    return Optional.ofNullable(registry.get(view));
  }
}
