package nl.knaw.huc.service.index;

import nl.knaw.huc.core.TextRepoFile;

import javax.annotation.Nonnull;

public interface IndexService {

  /**
   * Index file with latest version contents
   * Use blank string when no versions available
   */
  void index(@Nonnull TextRepoFile file);

}
