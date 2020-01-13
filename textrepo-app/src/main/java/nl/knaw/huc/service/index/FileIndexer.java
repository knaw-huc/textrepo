package nl.knaw.huc.service.index;

import nl.knaw.huc.core.TextrepoFile;

import javax.annotation.Nonnull;

public interface FileIndexer {
  void indexFile(
      @Nonnull TextrepoFile file,
      @Nonnull String latestVersionContent
  );
}
