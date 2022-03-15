package nl.knaw.huc.service.task;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;
import javax.ws.rs.NotFoundException;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetLatestFileVersion implements InTransactionProvider<Version> {
  private static final Logger log = LoggerFactory.getLogger(GetLatestFileVersion.class);

  private final TextRepoFile file;

  public GetLatestFileVersion(TextRepoFile file) {
    this.file = requireNonNull(file);
  }

  @Override
  public Version executeIn(Handle transaction) {
    return transaction.attach(VersionsDao.class)
                      .findLatestByFileId(file.getId())
                      .orElseThrow(noLatestVersionFound(file));
  }

  private Supplier<NotFoundException> noLatestVersionFound(TextRepoFile file) {
    return () -> {
      final var message = String.format("No latest version found for file: %s", file.getId());
      log.warn(message);
      return new NotFoundException(message);
    };
  }

}
