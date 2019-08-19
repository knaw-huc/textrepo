package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;

public interface FileStoreService {
  void storeFile(TextRepoFile file);

  TextRepoFile getBySha224(String sha224);
}
