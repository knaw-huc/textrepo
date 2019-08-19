package nl.knaw.huc.service.store;

import nl.knaw.huc.api.TextRepoFile;

public interface FileStorage {
  void storeFile(TextRepoFile file);

  TextRepoFile getBySha224(String sha224);
}
