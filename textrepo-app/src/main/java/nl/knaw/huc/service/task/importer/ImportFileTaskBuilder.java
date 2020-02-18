package nl.knaw.huc.service.task.importer;

import nl.knaw.huc.service.task.Task;

public interface ImportFileTaskBuilder {
  ImportFileTaskBuilder forExternalId(String externalId);

  ImportFileTaskBuilder withType(String type);

  ImportFileTaskBuilder forFilename(String name);

  ImportFileTaskBuilder withContents(byte[] contents);

  Task build();
}
