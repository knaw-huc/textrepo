package nl.knaw.huc.service.store;

import nl.knaw.huc.api.TextRepoFile;

import javax.annotation.Nonnull;

public interface FileStorage {
  void storeFile(@Nonnull TextRepoFile file);

  TextRepoFile getBySha224(@Nonnull String sha224);
}
