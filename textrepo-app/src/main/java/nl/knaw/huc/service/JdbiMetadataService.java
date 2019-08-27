package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.KeyValue;
import nl.knaw.huc.db.MetadataDao;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nonnull;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public class JdbiMetadataService implements MetadataService {
  private final Jdbi jdbi;

  public JdbiMetadataService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public List<KeyValue> getMetadata(UUID documentId) {
    return this
        .find(documentId)
        .stream()
        .map(entry -> new KeyValue(entry.getKey(), entry.getValue()))
        .collect(toList());
  }

  @Override
  public void addMetadata(@Nonnull UUID documentId, @Nonnull List<KeyValue> metadata) {
    var entries = metadata
        .stream()
        .map(kv -> new MetadataEntry(documentId, kv.key, kv.value))
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
  public List<MetadataEntry> find(@Nonnull UUID documentId) {
    return getMetadataDao().findByDocumentUuid(documentId);
  }

  private MetadataDao getMetadataDao() {
    return jdbi.onDemand(MetadataDao.class);
  }
}
