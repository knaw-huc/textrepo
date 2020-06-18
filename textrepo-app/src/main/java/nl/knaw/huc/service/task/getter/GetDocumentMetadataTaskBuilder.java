package nl.knaw.huc.service.task.getter;

import nl.knaw.huc.core.DocumentMetadata;
import nl.knaw.huc.service.task.Task;

public interface GetDocumentMetadataTaskBuilder {
  GetDocumentMetadataTaskBuilder forExternalId(String externalId);

  Task<DocumentMetadata> build();
}
