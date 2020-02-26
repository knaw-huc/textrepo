package nl.knaw.huc.service;

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

}
