package nl.knaw.huc.service.task.importfile;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.ContentsDao;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Handle;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.time.LocalDateTime.now;

class SetCurrentFileContents implements Function<TextrepoFile, Version> {
  private final Handle transaction;
  private final Contents contents;

  SetCurrentFileContents(Handle transaction, Contents contents) {
    this.transaction = transaction;
    this.contents = contents;
  }

  @Override
  public Version apply(TextrepoFile file) {
    return latestVersionIfIdentical(file)
        .orElseGet(createNewVersionWithContents(file));
  }

  private Optional<Version> latestVersionIfIdentical(TextrepoFile file) {
    return versions().findLatestByFileId(file.getId())
                     .filter(hasIdenticalContents());
  }

  private Predicate<Version> hasIdenticalContents() {
    return candidate -> candidate.getContentsSha().equals(contents.getSha224());
  }

  private Supplier<Version> createNewVersionWithContents(TextrepoFile file) {
    return () -> {
      final var version = new Version(file.getId(), now(), contents.getSha224());
      contents().insert(contents);
      versions().insert(version);
      return version;
    };
  }

  private VersionsDao versions() {
    return transaction.attach(VersionsDao.class);
  }

  private ContentsDao contents() {
    return transaction.attach(ContentsDao.class);
  }
}
