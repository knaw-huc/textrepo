package nl.knaw.huc.service.index;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.util.UUID;

public interface DocumentIndexer {
  void indexDocument(@Nonnull UUID documentId, @NotNull String latestVersionContent);
}
