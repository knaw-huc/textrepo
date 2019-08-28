package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.db.MetadataDao;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    var entries = metadata
        .entrySet()
        .stream()
        .map(kv -> new MetadataEntry(documentId, kv.getKey(), kv.getValue()))
        .iterator();
    bulkInsert(entries);
  }

  @Override
  public void insert(@Nonnull MetadataEntry entry) {
    getMetadataDao().insert(entry);
  }

  @Override
  public void bulkInsert(@Nonnull Iterator<MetadataEntry> entries) {
    getMetadataDao().bulkInsert(entries);
  }

  @Override
  public void update(MetadataEntry entry) {
    getMetadataDao().update(entry);
  }

  @Override
  public Iterator<MetadataEntry> find(@Nonnull UUID documentId) {
    return getMetadataDao().findByDocumentUuid(documentId);
  }

  private MetadataDao getMetadataDao() {
    return jdbi.onDemand(MetadataDao.class);
  }
}
