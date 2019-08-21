package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.service.store.FileStorage;

public class FileService {
  private final FileStorage fileStorage;

  public FileService(FileStorage fileStorage) {
    this.fileStorage = fileStorage;
  }

  public void addFile(TextRepoFile file) {
    fileStorage.storeFile(file);
  }

  public TextRepoFile getBySha224(String sha224) {
    return fileStorage.getBySha224(sha224);
  }
}
