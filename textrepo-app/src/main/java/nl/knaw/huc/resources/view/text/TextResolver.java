package nl.knaw.huc.resources.view.text;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.resources.view.RangeResolver;

public abstract class TextResolver<R> extends RangeResolver<Contents, R> {
  public static final String LINEBREAK_MATCHER = "\\R";
}
