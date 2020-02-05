package nl.knaw.huc.service.task;

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
import static java.util.Objects.requireNonNull;

public class SetCurrentFileContents implements Function<Handle, Version> {
  private final TextrepoFile file;
  private final Contents contents;

  private Handle transaction;

  public SetCurrentFileContents(TextrepoFile file, Contents contents) {
    this.file = requireNonNull(file);
    this.contents = requireNonNull(contents);
  }

  @Override
  public Version apply(Handle transaction) {
    this.transaction = requireNonNull(transaction);
    return latestVersionIfIdentical().orElseGet(createNewVersionWithContents());
  }

  private Optional<Version> latestVersionIfIdentical() {
    return versions().findLatestByFileId(file.getId())
                     .filter(hasIdenticalContents());
  }

  private Predicate<Version> hasIdenticalContents() {
    return candidate -> candidate.getContentsSha().equals(contents.getSha224());
  }

  private Supplier<Version> createNewVersionWithContents() {
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
