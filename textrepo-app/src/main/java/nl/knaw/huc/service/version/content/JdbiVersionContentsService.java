package nl.knaw.huc.service.version.content;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.db.ContentsDao;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.NotFoundException;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;

public class JdbiVersionContentsService implements VersionContentsService {

  private final Jdbi jdbi;

  public JdbiVersionContentsService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Contents getByVersionId(UUID versionId) {
    return contents()
        .findByVersionId(versionId)
        .orElseThrow(() -> new NotFoundException(format("No contents found for version %s", versionId)));
  }

  @Override
  public Optional<String> getVersionMimetype(UUID versionId) {
    return versions().findMimetypeByVersionId(versionId);
  }

  private ContentsDao contents() {
    return jdbi.onDemand(ContentsDao.class);
  }

  private VersionsDao versions() {
    return jdbi.onDemand(VersionsDao.class);
  }

}
