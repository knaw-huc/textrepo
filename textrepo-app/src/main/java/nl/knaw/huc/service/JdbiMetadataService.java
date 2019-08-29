package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.db.MetadataDao;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class JdbiMetadataService implements MetadataService {
  private final Jdbi jdbi;

  public JdbiMetadataService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Map<String, String> getMetadata(UUID documentId) {
    final var result = new HashMap<String, String>();
    find(documentId).forEachRemaining(entry -> result.put(entry.getKey(), entry.getValue()));
    return result;
  }

  @Override
  public void addMetadata(@Nonnull UUID documentId, @Nonnull Map<String, String> metadata) {
    getMetadataDao().bulkInsert(documentId, metadata.entrySet().iterator());
  }

  @Override
  public void insert(@Nonnull UUID documentId, @Nonnull MetadataEntry entry) {
    getMetadataDao().insert(documentId, entry);
  }

  @Override
  public void update(@Nonnull UUID documentId, MetadataEntry entry) {
    getMetadataDao().update(documentId, entry);
  }

  @Override
  public Iterator<MetadataEntry> find(@Nonnull UUID documentId) {
    return getMetadataDao().findByDocumentUuid(documentId);
  }

  private MetadataDao getMetadataDao() {
    return jdbi.onDemand(MetadataDao.class);
  }
}
