package nl.knaw.huc.resources.view;

import io.dropwizard.jersey.params.AbstractParam;
import java.util.OptionalInt;
import javax.annotation.Nullable;

public class RangeParam extends AbstractParam<OptionalInt> {
  public RangeParam(@Nullable String input) {
    super(input);
  }

  public RangeParam(@Nullable String input, String parameterName) {
    super(input, parameterName);
  }

  @Override
  protected OptionalInt parse(@Nullable String input) {
    if ("full".equals(input)) {
      return OptionalInt.empty();
    }

    @SuppressWarnings("all") // parseUnsignedInt handles null input
    final var value = Integer.parseUnsignedInt(input);

    return OptionalInt.of(value);
  }

  @Override
  protected String errorMessage(Exception err) {
    return "%s must either be an integer >= 0, or 'full' (meaning no range constraint)";
  }
}
