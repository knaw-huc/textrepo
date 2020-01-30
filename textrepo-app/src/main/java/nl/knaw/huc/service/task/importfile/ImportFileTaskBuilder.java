package nl.knaw.huc.service.task.importfile;

import nl.knaw.huc.service.task.Task;

import java.io.InputStream;

public interface ImportFileTaskBuilder {
  ImportFileTaskBuilder forExternalId(String externalId);

  ImportFileTaskBuilder withType(String type);

  ImportFileTaskBuilder forFilename(String name);

  ImportFileTaskBuilder withContents(InputStream inputStream);

  Task build();
}
