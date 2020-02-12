package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.ContentsDao;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class GetVersionContent implements ProvidesInTransaction<Contents> {
  private static final Logger LOG = LoggerFactory.getLogger(GetVersionContent.class);

  private final Version version;

  private Handle transaction;

  public GetVersionContent(Version version) {
    this.version = requireNonNull(version);
  }

  @Override
  public Contents executeIn(Handle transaction) {
    this.transaction = requireNonNull(transaction);
    return contents().findBySha224(version.getContentsSha())
                     .orElseThrow(noContentsFoundForVersion(version));
  }

  private Supplier<NotFoundException> noContentsFoundForVersion(Version latest) {
    return () -> {
      final var message = String.format("No contents found for version of file %s, sha224=%s",
          version.getFileId(), latest.getContentsSha());
      LOG.warn(message);
      return new NotFoundException(message);
    };
  }

  private ContentsDao contents() {
    return transaction.attach(ContentsDao.class);
  }
}
