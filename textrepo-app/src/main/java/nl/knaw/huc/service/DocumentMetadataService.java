package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.Document;

import java.util.Map;
import java.util.UUID;

public interface DocumentMetadataService {
  Map<String, String> getByDocId(UUID docId);

  boolean update(UUID docId, MetadataEntry metadataEntry);
}
