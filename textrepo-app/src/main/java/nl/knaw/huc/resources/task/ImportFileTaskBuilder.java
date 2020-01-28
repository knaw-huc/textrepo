package nl.knaw.huc.resources.task;

import java.io.InputStream;

public interface ImportFileTaskBuilder {
  ImportFileTaskBuilder forExternalId(String externalId);

  ImportFileTaskBuilder withType(String type);

  ImportFileTaskBuilder forFilename(String name);

  ImportFileTaskBuilder withContents(InputStream inputStream);

  Task build();
}
