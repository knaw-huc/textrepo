package nl.knaw.huc.service.index;

import nl.knaw.huc.core.TextRepoFile;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.util.UUID;

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

}
