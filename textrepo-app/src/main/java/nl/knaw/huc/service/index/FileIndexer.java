package nl.knaw.huc.service.index;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.util.UUID;

public interface FileIndexer {
  void indexFile(@Nonnull UUID fileId, @NotNull String latestVersionContent);
}
