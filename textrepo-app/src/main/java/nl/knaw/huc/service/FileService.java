package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;

public class FileService {
  private final FileStoreService store;
  private final FileIndexService index;

  public FileService(FileStoreService store, FileIndexService index) {
    this.store = store;
    this.index = index;
  }

  public void addFile(TextRepoFile file) {
    store.storeFile(file);
    index.indexFile(file);
  }

  public TextRepoFile getBySha224(String sha224) {
    return store.getBySha224(sha224);
  }
}
