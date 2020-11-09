package nl.knaw.huc.service.version.metadata;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.db.VersionMetadataDao;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static nl.knaw.huc.helpers.PsqlExceptionHelper.Constraint.FILES_METADATA_FILE_ID_FKEY;
import static nl.knaw.huc.helpers.PsqlExceptionHelper.violatesConstraint;

public class JdbiVersionMetadataService implements VersionMetadataService {
  private final Jdbi jdbi;

  public JdbiVersionMetadataService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Map<String, String> getMetadata(UUID versionId) {
    return metadata().getMetadataByVersionId(versionId);
  }

  @Override
  public void insert(@Nonnull UUID versionId, @Nonnull MetadataEntry entry) {
    metadata().insert(versionId, entry);
  }

  @Override
  public void upsert(@Nonnull UUID versionId, MetadataEntry entry) {
    try {
      metadata().upsert(versionId, entry);
    } catch (JdbiException ex) {
      if (violatesConstraint(ex, FILES_METADATA_FILE_ID_FKEY)) {
        throw new NotFoundException(format("No such version: %s", versionId));
      } else {
        throw (ex);
      }
    }
  }

  @Override
  public void delete(UUID versionId, String key) {
    metadata().delete(versionId, key);
  }

  private VersionMetadataDao metadata() {
    return jdbi.onDemand(VersionMetadataDao.class);
  }
}
