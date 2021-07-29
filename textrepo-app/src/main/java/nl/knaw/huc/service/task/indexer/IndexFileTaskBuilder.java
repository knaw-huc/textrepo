package nl.knaw.huc.service.task.indexer;

import nl.knaw.huc.service.task.Task;

public interface IndexFileTaskBuilder {

  /**
   * Index by externalId and Type
   */
  IndexFileTaskBuilder forExternalId(String externalId);
  IndexFileTaskBuilder withType(String type);

  /**
   * Index by index name
   */
  IndexFileTaskBuilder forIndex(String name);

  Task<String> build();
}
