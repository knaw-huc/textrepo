package nl.knaw.huc.service.task.getter;

import nl.knaw.huc.service.task.Task;

import java.util.Map;

public interface GetDocumentMetadataTaskBuilder {
  GetDocumentMetadataTaskBuilder forExternalId(String externalId);

  Task<Map<String, String>> build();
}
