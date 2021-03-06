package nl.knaw.huc.service.task;

import com.fasterxml.jackson.databind.PropertyMetadata;
import nl.knaw.huc.service.task.deleter.DeleteDocumentTaskBuilder;
import nl.knaw.huc.service.task.finder.FindContentsTaskBuilder;
import nl.knaw.huc.service.task.getter.GetDocumentMetadataTaskBuilder;
import nl.knaw.huc.service.task.getter.GetFileMetadataTaskBuilder;
import nl.knaw.huc.service.task.importer.ImportFileTaskBuilder;
import nl.knaw.huc.service.task.indexer.IndexFileTaskBuilder;

public interface TaskBuilderFactory {
  ImportFileTaskBuilder getDocumentImportBuilder();

  IndexFileTaskBuilder getIndexBuilder();

  FindContentsTaskBuilder getContentsFinderBuilder();

  DeleteDocumentTaskBuilder getDocumentDeleteBuilder();

  GetDocumentMetadataTaskBuilder getDocumentMetadataGetter();

  GetFileMetadataTaskBuilder getFileMetadataGetter();

  RegisterIdentifiersTaskBuilder getRegisterIdentifiersBuilder();

}
