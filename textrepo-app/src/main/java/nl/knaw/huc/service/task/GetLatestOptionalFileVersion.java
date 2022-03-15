package nl.knaw.huc.service.task;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetLatestOptionalFileVersion implements InTransactionProvider<Optional<Version>> {
  private static final Logger log = LoggerFactory.getLogger(GetLatestOptionalFileVersion.class);

  private final TextRepoFile file;

  public GetLatestOptionalFileVersion(TextRepoFile file) {
    this.file = requireNonNull(file);
  }

  @Override
  public Optional<Version> executeIn(Handle transaction) {
    return transaction
        .attach(VersionsDao.class)
        .findLatestByFileId(file.getId());
  }

}
