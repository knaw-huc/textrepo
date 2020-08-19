package nl.knaw.huc.service.file.metadata;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.db.FileMetadataDao;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

public class JdbiFileMetadataService implements FileMetadataService {
  private final Jdbi jdbi;

  public JdbiFileMetadataService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Map<String, String> getMetadata(UUID fileId) {
    return metadata().getMetadataByFileId(fileId);
  }

  @Override
  public void insert(@Nonnull UUID fileId, @Nonnull MetadataEntry entry) {
    metadata().insert(fileId, entry);
  }

  @Override
  public void upsert(@Nonnull UUID fileId, MetadataEntry entry) {
    metadata().upsert(fileId, entry);
  }

  @Override
  public void delete(UUID fileId, String key) {
    metadata().delete(fileId, key);
  }

  private FileMetadataDao metadata() {
    return jdbi.onDemand(FileMetadataDao.class);
  }
}
