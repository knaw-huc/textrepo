package nl.knaw.huc.service.index;

import nl.knaw.huc.core.TextRepoFile;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public interface Indexer {
  Optional<String> index(
      @Nonnull TextRepoFile file,
      @Nonnull String latestVersionContents
  );

  IndexerConfiguration getConfig();

  /**
   * List of mimetypes supported by indexer
   * When not present, indexer is assumed to support al mimetypes
   */
  Optional<List<String>> getMimetypes();
}
