package nl.knaw.huc.service;

import java.util.Map;
import java.util.UUID;

public interface DocumentService {
  UUID createDocument(UUID fileId);

  UUID findFileForType(UUID docId, String fileType);

  Map<String, String> getMetadata(UUID docId);
}
