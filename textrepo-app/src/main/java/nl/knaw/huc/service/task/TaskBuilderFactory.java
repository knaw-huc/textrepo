package nl.knaw.huc.service.task;

import nl.knaw.huc.service.task.importfile.ImportFileTaskBuilder;
import nl.knaw.huc.service.task.indexfile.IndexFileTaskBuilder;

public interface TaskBuilderFactory {
  ImportFileTaskBuilder getDocumentImportBuilder();

  IndexFileTaskBuilder getDocumentIndexBuilder();
}
