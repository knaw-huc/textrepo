package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.db.MetadataDao;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbiMetadataService implements MetadataService {
  private final Jdbi jdbi;

  public JdbiMetadataService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public void insert(MetadataEntry entry) {
    getMetadataDao().insert(entry);
  }

  @Override
  public void bulkInsert(@Nonnull List<MetadataEntry> entries) {
    getMetadataDao().bulkInsert(entries);
  }

  @Override
  public Optional<MetadataEntry> find(@Nonnull UUID documentId, @Nonnull String key) {
    return getMetadataDao().findByDocumentUuidAndKey(documentId, key);
  }

  private MetadataDao getMetadataDao() {
    return jdbi.onDemand(MetadataDao.class);
  }
}
