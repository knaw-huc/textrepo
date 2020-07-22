package nl.knaw.huc.service;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.db.ContentsDao;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.NotFoundException;
import java.util.UUID;

import static java.lang.String.format;

public class JdbiVersionContentsService implements VersionContentsService {

  private final Jdbi jdbi;

  public JdbiVersionContentsService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Contents getByVersionId(UUID versionId) {
    return versions()
        .findByVersionId(versionId)
        .orElseThrow(() -> new NotFoundException(format("No contents found for version %s", versionId)));
  }

  private ContentsDao versions() {
    return jdbi.onDemand(ContentsDao.class);
  }

}
