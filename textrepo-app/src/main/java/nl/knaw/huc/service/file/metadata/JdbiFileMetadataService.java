package nl.knaw.huc.service.file.metadata;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.db.FileMetadataDao;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static nl.knaw.huc.helpers.PsqlExceptionHelper.Constraint.FILES_METADATA_FILE_ID_FKEY;
import static nl.knaw.huc.helpers.PsqlExceptionHelper.violatesConstraint;

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
    try {
      metadata().upsert(fileId, entry);
    } catch (JdbiException ex) {
      if (violatesConstraint(ex, FILES_METADATA_FILE_ID_FKEY)) {
        throw new NotFoundException(format("No such file: %s", fileId));
      } else {
        throw (ex);
      }
    }
  }

  @Override
  public void delete(UUID fileId, String key) {
    metadata().delete(fileId, key);
  }

  private FileMetadataDao metadata() {
    return jdbi.onDemand(FileMetadataDao.class);
  }
}
