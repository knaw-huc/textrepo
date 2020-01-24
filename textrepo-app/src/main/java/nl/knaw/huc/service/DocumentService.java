package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.TextrepoFile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface DocumentService {
  UUID createDocument(UUID fileId, String externalId);

  void addFileToDocument(UUID docId, UUID fileId);

  TextrepoFile findFileForType(UUID docId, String fileType);

  Map<String, String> getMetadata(UUID docId);

  boolean updateMetadata(UUID docId, MetadataEntry metadataEntry);

  Optional<UUID> findDocumentByFilename(String filename);
}
