package nl.knaw.huc.service.index;

import nl.knaw.huc.api.TextRepoFile;

public interface FileIndexer {
  void indexFile(TextRepoFile file);
}
