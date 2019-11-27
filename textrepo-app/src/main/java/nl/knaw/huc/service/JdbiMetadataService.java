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
  public Map<String, String> getMetadata(UUID fileId) {
    final var result = new HashMap<String, String>();
    find(fileId).forEachRemaining(entry -> result.put(entry.getKey(), entry.getValue()));
    return result;
  }

  @Override
  public void addMetadata(@Nonnull UUID fileId, @Nonnull Map<String, String> metadata) {
    getMetadataDao().bulkInsert(fileId, metadata.entrySet().iterator());
  }

  @Override
  public void insert(@Nonnull UUID fileId, @Nonnull MetadataEntry entry) {
    getMetadataDao().insert(fileId, entry);
  }

  @Override
  public void update(@Nonnull UUID fileId, MetadataEntry entry) {
    getMetadataDao().update(fileId, entry);
  }

  @Override
  public Iterator<MetadataEntry> find(@Nonnull UUID fileId) {
    return getMetadataDao().findByDocumentId(fileId);
  }

  private MetadataDao getMetadataDao() {
    return jdbi.onDemand(MetadataDao.class);
  }
}
