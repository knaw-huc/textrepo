package nl.knaw.huc.service.task;

import nl.knaw.huc.service.task.importfile.ImportFileTaskBuilder;

public interface TaskBuilderFactory {
  ImportFileTaskBuilder getDocumentImportBuilder();
}
