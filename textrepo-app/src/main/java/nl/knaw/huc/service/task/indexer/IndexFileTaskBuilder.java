package nl.knaw.huc.service.task.indexer;

import nl.knaw.huc.service.task.Task;

public interface IndexFileTaskBuilder {

  /**
   * Index file by externalId and Type
   */
  IndexFileTaskBuilder forExternalId(String externalId);

  /**
   * Index file by externalId and Type
   */
  IndexFileTaskBuilder withType(String type);

  /**
   * Index files by index name
   */
  IndexFileTaskBuilder forIndex(String name);

  Task<String> build();
}
