package nl.knaw.huc.service.index;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface FileIndexer {
  void indexFile(
      @Nonnull UUID fileId,
      @Nonnull String latestVersionContent
  );
}
