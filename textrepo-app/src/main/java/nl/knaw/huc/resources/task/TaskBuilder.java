package nl.knaw.huc.resources.task;

import java.io.InputStream;

public interface TaskBuilder {
  TaskBuilder forExternalId(String externalId);

  TaskBuilder withType(String type);

  TaskBuilder forFilename(String name);

  TaskBuilder withContents(InputStream inputStream);

  Task build();
}
