package nl.knaw.huc.service.document.metadata;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import nl.knaw.huc.api.MetadataEntry;

public interface DocumentMetadataService {
  void create(UUID docId, MetadataEntry metadataEntry);

  Map<String, String> getByDocId(UUID docId);

  boolean upsert(UUID docId, MetadataEntry metadataEntry);

  void delete(UUID docId, String key);

  List<UUID> findByMetadataKey(String key);
}
