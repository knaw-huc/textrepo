package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.db.MetadataDao;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public class JdbiMetadataService implements MetadataService {
  private final Jdbi jdbi;

  public JdbiMetadataService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public List<DocumentService.KeyValue> getMetadata(UUID documentId) {
    return this
        .find(documentId)
        .stream()
        .map(entry -> new DocumentService.KeyValue(entry.getKey(), entry.getValue()))
        .collect(toList());
  }

  @Override
  public void addMetadata(@Nonnull UUID documentId, @Nonnull List<DocumentService.KeyValue> metadata) {
    var entries = metadata.stream().map(kv -> new MetadataEntry(documentId, kv.key, kv.value)).collect(toList());
    bulkInsert(entries);
  }

  public void addMetadata(@Nonnull UUID documentId, @Nonnull String key, @Nonnull String value) {
    insert(new MetadataEntry(documentId, key, value));
  }

  @Override
  public void insert(@Nonnull MetadataEntry entry) {
    getMetadataDao().insert(entry);
  }

  @Override
  public void bulkInsert(@Nonnull List<MetadataEntry> entries) {
    getMetadataDao().bulkInsert(entries);
  }

  @Override
  public List<MetadataEntry> find(@Nonnull UUID documentId) {
    return getMetadataDao().findByDocumentUuid(documentId);
  }

  private MetadataDao getMetadataDao() {
    return jdbi.onDemand(MetadataDao.class);
  }
}