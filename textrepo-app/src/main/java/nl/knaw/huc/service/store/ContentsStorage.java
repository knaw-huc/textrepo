package nl.knaw.huc.service.store;

import nl.knaw.huc.api.TextRepoContents;

import javax.annotation.Nonnull;

public interface ContentsStorage {
  void storeContents(@Nonnull TextRepoContents contents);

  TextRepoContents getBySha224(@Nonnull String sha224);
}
