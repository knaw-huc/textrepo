package nl.knaw.huc.service.task;

import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class GetLatestFileVersion implements InTransactionProvider<Version> {
  private static final Logger LOG = LoggerFactory.getLogger(GetLatestFileVersion.class);

  private final TextrepoFile file;

  public GetLatestFileVersion(TextrepoFile file) {
    this.file = requireNonNull(file);
  }

  @Override
  public Version executeIn(Handle transaction) {
    return transaction.attach(VersionsDao.class)
                      .findLatestByFileId(file.getId())
                      .orElseThrow(noLatestVersionFound(file));
  }

  private Supplier<NotFoundException> noLatestVersionFound(TextrepoFile file) {
    return () -> {
      final var message = String.format("No latest version found for file: %s", file.getId());
      LOG.warn(message);
      return new NotFoundException(message);
    };
  }

}
