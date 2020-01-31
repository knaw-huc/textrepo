package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.db.FileMetadataDao;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

public class JdbiFileMetadataService implements MetadataService {
  private final Jdbi jdbi;

  public JdbiFileMetadataService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Map<String, String> getMetadata(UUID fileId) {
    return getMetadataDao().getMetadataByFileId(fileId);
  }

  @Override
  public void addMetadata(@Nonnull UUID fileId, @Nonnull Map<String, String> metadata) {
    getMetadataDao().bulkInsert(fileId, metadata.entrySet().iterator());
  }

  @Override
  public void insert(@Nonnull UUID fileId, @Nonnull MetadataEntry entry) {
    getMetadataDao().insertFileMetadata(fileId, entry);
  }

  @Override
  public void update(@Nonnull UUID fileId, MetadataEntry entry) {
    getMetadataDao().updateFileMetadata(fileId, entry);
  }

  private FileMetadataDao getMetadataDao() {
    return jdbi.onDemand(FileMetadataDao.class);
  }
}
