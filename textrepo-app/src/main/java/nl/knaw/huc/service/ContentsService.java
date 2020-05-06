package nl.knaw.huc.service;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.service.store.ContentsStorage;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.copyOfRange;

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

  public static String abbreviate(byte[] contents) {
    var replacement = "[..]";
    var lengthStartEnd = 48;
    var start = new String(copyOfRange(contents, 0, lengthStartEnd), UTF_8);
    var end = new String(copyOfRange(contents, contents.length - lengthStartEnd, contents.length), UTF_8);
    return start + replacement + end;
  }

}
