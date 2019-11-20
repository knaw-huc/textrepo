package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoContents;
import nl.knaw.huc.service.store.ContentsStorage;

public class ContentsService {
  private final ContentsStorage contentsStorage;

  public ContentsService(ContentsStorage contentsStorage) {
    this.contentsStorage = contentsStorage;
  }

  public void addContents(TextRepoContents contents) {
    contentsStorage.storeContents(contents);
  }

  public TextRepoContents getBySha224(String sha224) {
    return contentsStorage.getBySha224(sha224);
  }
}
