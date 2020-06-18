package nl.knaw.huc.service.task.getter;

import nl.knaw.huc.core.FileMetadata;
import nl.knaw.huc.service.task.Task;

public interface GetFileMetadataTaskBuilder {
  GetFileMetadataTaskBuilder forExternalId(String externalId);

  GetFileMetadataTaskBuilder forType(String typeName);

  Task<FileMetadata> build();
}
