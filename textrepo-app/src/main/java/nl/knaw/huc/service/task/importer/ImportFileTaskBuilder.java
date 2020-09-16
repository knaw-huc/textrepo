package nl.knaw.huc.service.task.importer;

import nl.knaw.huc.core.Version;
import nl.knaw.huc.service.task.Task;

import java.io.InputStream;

public interface ImportFileTaskBuilder {
  ImportFileTaskBuilder allowNewDocument(boolean allowNewDocument);

  ImportFileTaskBuilder forExternalId(String externalId);

  ImportFileTaskBuilder withTypeName(String type);

  ImportFileTaskBuilder forFilename(String name);

  ImportFileTaskBuilder withContents(byte[] contents);

  Task<Version> build();

  ImportFileTaskBuilder withInputStream(InputStream inputStream);
}
