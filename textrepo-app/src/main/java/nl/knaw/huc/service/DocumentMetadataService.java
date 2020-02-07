package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;

import java.util.Map;
import java.util.UUID;

public interface DocumentMetadataService {
  void create(UUID docId, MetadataEntry metadataEntry);

  Map<String, String> getByDocId(UUID docId);

  boolean upsert(UUID docId, MetadataEntry metadataEntry);

  void delete(UUID docId, String key);
}
