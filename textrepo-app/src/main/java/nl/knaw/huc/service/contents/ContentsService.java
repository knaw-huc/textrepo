package nl.knaw.huc.service.contents;

import static java.nio.charset.StandardCharsets.UTF_8;

import javax.annotation.Nonnull;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.service.store.ContentsStorage;

public class ContentsService {
  private final ContentsStorage contentsStorage;

  public ContentsService(ContentsStorage contentsStorage) {
    this.contentsStorage = contentsStorage;
  }

  public void addContents(Contents contents) {
    contentsStorage.storeContents(contents);
  }

  public Contents getBySha(String sha) {
    return contentsStorage.get(sha);
  }

  /**
   * Abbreviate byte[] to String of 100 chars, replacing the middle with [..]
   *
   * <p>Like org.apache.commons.lang3.StringUtils.StringUtils.abbreviateMiddle
   * but then without having to convert byte[] to String
   *
   * <p>TODO: properly handle multi-byte UTF sequences (TT-606).
   */
  public static String abbreviateMiddle(@Nonnull byte[] contents) {
    var replacement = "[..]";
    var lengthStartEnd = 48;

    if (contents.length <= 2 * lengthStartEnd + replacement.length()) {
      return new String(contents, UTF_8);
    }

    var start = new String(contents, 0, lengthStartEnd, UTF_8);
    var end = new String(contents, contents.length - lengthStartEnd, lengthStartEnd, UTF_8);
    return start + replacement + end;
  }

}
