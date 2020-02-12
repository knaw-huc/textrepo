package nl.knaw.huc.service.task.indexfile;

import nl.knaw.huc.service.task.Task;

public interface IndexFileTaskBuilder {
  IndexFileTaskBuilder forExternalId(String externalId);

  IndexFileTaskBuilder withType(String type);

  Task<String> build();
}
