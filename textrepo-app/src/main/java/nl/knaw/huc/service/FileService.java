package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.service.index.FileIndexer;
import nl.knaw.huc.service.store.FileStorage;

public class FileService {
  private final FileStorage fileStorage;
  private final FileIndexer fileIndexer;

  public FileService(FileStorage fileStorage, FileIndexer fileIndexer) {
    this.fileStorage = fileStorage;
    this.fileIndexer = fileIndexer;
  }

  public void addFile(TextRepoFile file) {
    fileStorage.storeFile(file);
    fileIndexer.indexFile(file);
  }

  public TextRepoFile getBySha224(String sha224) {
    return fileStorage.getBySha224(sha224);
  }
}
