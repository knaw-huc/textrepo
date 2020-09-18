package nl.knaw.huc.service.document.metadata;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.db.DocumentMetadataDao;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JdbiDocumentMetadataService implements DocumentMetadataService {
  private final Jdbi jdbi;

  public JdbiDocumentMetadataService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public void create(UUID docId, MetadataEntry metadataEntry) {
    metadata().insert(docId, metadataEntry);
  }

  @Override
  public Map<String, String> getByDocId(UUID docId) {
    return metadata().getByDocumentId(docId);
  }

  @Override
  public boolean upsert(UUID docId, MetadataEntry entry) {
    return metadata().upsert(docId, entry);
  }

  @Override
  public void delete(UUID docId, String key) {
    metadata().delete(docId, key);
  }

  @Override
  public List<UUID> findByMetadataKey(String key) {
    return metadata().findByMetadataKey(key);
  }

  private DocumentMetadataDao metadata() {
    return jdbi.onDemand(DocumentMetadataDao.class);
  }

}