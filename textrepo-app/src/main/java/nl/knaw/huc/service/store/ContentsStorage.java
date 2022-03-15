package nl.knaw.huc.service.store;

import javax.annotation.Nonnull;
import nl.knaw.huc.core.Contents;

public interface ContentsStorage {
  void storeContents(@Nonnull Contents contents);

  Contents get(@Nonnull String sha);

}
