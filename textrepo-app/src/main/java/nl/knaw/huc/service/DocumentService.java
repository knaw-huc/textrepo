package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface DocumentService {
  UUID createDocument(UUID fileId);

  UUID findFileForType(UUID docId, String fileType);

  Map<String, String> getMetadata(UUID docId);

  boolean updateMetadata(UUID docId, MetadataEntry metadataEntry);

  Optional<UUID> findDocumentByFilename(String filename);
}
