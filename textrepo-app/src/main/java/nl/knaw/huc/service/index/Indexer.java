package nl.knaw.huc.service.index;

import nl.knaw.huc.core.TextRepoFile;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface Indexer {
  Optional<String> index(
      @Nonnull TextRepoFile file,
      @Nonnull String latestVersionContents
  );

  IndexerConfiguration getConfig();

}
