package nl.knaw.huc.resources.view;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import static java.lang.String.format;

public abstract class RangeResolver<S, R> {
  public abstract R resolve(S source);

  protected void checkOffsets(int start, int end, int limit) {
    if (end < start) {
      throw new BadRequestException(
          format("endOffset must be >= startOffset (%d), but is: %d", start, end));
    }

    if (start > limit) {
      throw new NotFoundException(
          format("startOffset is limited by source text; must be <= %d, but is: %d", limit, start));
    }

    if (end > limit) {
      throw new NotFoundException(
          format("endOffset is limited by source text; must be <= %d, but is: %d", limit, end));
    }
  }
}
