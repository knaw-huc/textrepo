package nl.knaw.huc.service.task.importer;

import java.io.InputStream;
import nl.knaw.huc.api.ResultImportDocument;
import nl.knaw.huc.service.task.Task;

public interface ImportFileTaskBuilder {
  ImportFileTaskBuilder allowNewDocument(boolean allowNewDocument);

  ImportFileTaskBuilder asLatestVersion(boolean asLatestVersion);

  ImportFileTaskBuilder forExternalId(String externalId);

  ImportFileTaskBuilder withTypeName(String type);

  ImportFileTaskBuilder forFilename(String name);

  ImportFileTaskBuilder withContents(InputStream inputStream);

  ImportFileTaskBuilder withIndexing(boolean index);

  Task<ResultImportDocument> build();
}
