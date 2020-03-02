package nl.knaw.huc.service.index;

import nl.knaw.huc.core.TextrepoFile;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface Indexer {
  Optional<String> index(
      @Nonnull TextrepoFile file,
      @Nonnull String latestVersionContents
  );
}
