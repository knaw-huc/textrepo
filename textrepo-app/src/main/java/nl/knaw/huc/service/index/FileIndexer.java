package nl.knaw.huc.service.index;

import nl.knaw.huc.api.TextRepoFile;

import javax.annotation.Nonnull;

public interface FileIndexer {
  void indexFile(@Nonnull TextRepoFile file);
}
