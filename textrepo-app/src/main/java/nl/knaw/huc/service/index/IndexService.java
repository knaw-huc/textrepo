package nl.knaw.huc.service.index;

import nl.knaw.huc.core.TextRepoFile;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Handle index mutations
 */
public interface IndexService {

  /**
   * Index file with latest version contents
   * Use blank string when no versions available
   */
  void index(@Nonnull TextRepoFile file);

  /**
   * Index file with latest version contents
   * Use blank string when no versions available
   */
  void index(@Nonnull UUID fileId);

  /**
   * Index file with provided contents
   */
  void index(@Nonnull TextRepoFile file, @NotNull String contents);

  /**
   * Delete file from indices
   */
  void delete(UUID fileId);

  /**
   * Get all IDs from all indices
   */
  List<UUID> getAllIds();
}
