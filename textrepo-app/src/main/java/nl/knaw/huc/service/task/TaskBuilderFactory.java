package nl.knaw.huc.service.task;

import nl.knaw.huc.service.task.finder.FindContentsTaskBuilder;
import nl.knaw.huc.service.task.importer.ImportFileTaskBuilder;
import nl.knaw.huc.service.task.indexer.IndexFileTaskBuilder;

public interface TaskBuilderFactory {
  ImportFileTaskBuilder getDocumentImportBuilder();

  IndexFileTaskBuilder getDocumentIndexBuilder();

  FindContentsTaskBuilder getContentsFinderBuilder();
}
