package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.db.DocumentMetadataDao;
import org.jdbi.v3.core.Jdbi;

import java.util.Map;
import java.util.UUID;

public class JdbiDocumentMetadataService implements DocumentMetadataService {
  private final Jdbi jdbi;

  public JdbiDocumentMetadataService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public void create(UUID docId, MetadataEntry metadataEntry) {

  }

  @Override
  public Map<String, String> getByDocId(UUID docId) {
    return metadata().getByDocumentId(docId);
  }

  @Override
  public boolean update(UUID docId, MetadataEntry entry) {
    return metadata().update(docId, entry);
  }

  private DocumentMetadataDao metadata() {
    return jdbi.onDemand(DocumentMetadataDao.class);
  }

}
