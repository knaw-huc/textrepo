package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextrepoFile;

import javax.ws.rs.BadRequestException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface DocumentService {
  Document get(UUID doc);

  /**
   * Create a new document with a unique external ID
   * @throws BadRequestException when external ID already exists
   */
  UUID createDocument(String externalId);

  void addFileToDocument(UUID docId, UUID fileId);

  TextrepoFile findFileForType(UUID docId, String fileType);

  Map<String, String> getMetadata(UUID docId);

  boolean updateMetadata(UUID docId, MetadataEntry metadataEntry);

  Optional<UUID> findDocumentByExternalId(String externalId);
}
