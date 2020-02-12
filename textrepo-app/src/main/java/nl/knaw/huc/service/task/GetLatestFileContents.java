package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.ContentsDao;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class GetLatestFileContents implements ProvidesInTransaction<Contents> {
  private static final Logger LOG = LoggerFactory.getLogger(GetLatestFileContents.class);

  private final TextrepoFile file;

  private Handle transaction;

  public GetLatestFileContents(TextrepoFile file) {
    this.file = requireNonNull(file);
  }

  @Override
  public Contents executeIn(Handle transaction) {
    this.transaction = requireNonNull(transaction);
    final var latest = versions().findLatestByFileId(file.getId())
                                 .orElseThrow(noLatestVersionFound(file));

    return contents().findBySha224(latest.getContentsSha())
                     .orElseThrow(noContentsFoundForVersion(file, latest));
  }

  private Supplier<NotFoundException> noLatestVersionFound(TextrepoFile file) {
    return () -> {
      final var message = String.format("No latest version found for file: %s", file.getId());
      LOG.warn(message);
      return new NotFoundException(message);
    };
  }

  private Supplier<NotFoundException> noContentsFoundForVersion(TextrepoFile file, Version latest) {
    return () -> {
      final var message = String.format("No contents found for latest version of file %s, sha224=%s",
          file.getId(), latest.getContentsSha());
      LOG.warn(message);
      return new NotFoundException(message);
    };
  }

  private VersionsDao versions() {
    return transaction.attach(VersionsDao.class);
  }

  private ContentsDao contents() {
    return transaction.attach(ContentsDao.class);
  }
}
