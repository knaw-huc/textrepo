package nl.knaw.huc.service.store;

import nl.knaw.huc.core.Contents;

import javax.annotation.Nonnull;

public interface ContentsStorage {
  void storeContents(@Nonnull Contents contents);

  Contents get(@Nonnull String sha);

}
