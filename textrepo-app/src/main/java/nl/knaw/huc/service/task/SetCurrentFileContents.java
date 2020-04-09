package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.ContentsDao;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Handle;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static java.time.LocalDateTime.now;
import static java.util.Objects.requireNonNull;

public class SetCurrentFileContents implements InTransactionProvider<Version> {

  private Supplier<UUID> versionIdGenerator;
  private final TextrepoFile file;
  private final Contents contents;

  private Handle transaction;

  public SetCurrentFileContents(
      Supplier<UUID> versionIdGenerator,
      TextrepoFile file,
      Contents contents
  ) {
    this.versionIdGenerator = requireNonNull(versionIdGenerator);
    this.file = requireNonNull(file);
    this.contents = requireNonNull(contents);
  }

  @Override
  public Version executeIn(Handle transaction) {
    this.transaction = requireNonNull(transaction);
    return latestVersionIfIdentical().orElseGet(this::createNewVersionWithContents);
  }

  private Optional<Version> latestVersionIfIdentical() {
    return versions().findLatestByFileId(file.getId())
                     .filter(this::hasIdenticalContents);
  }

  private boolean hasIdenticalContents(Version candidate) {
    return candidate.getContentsSha().equals(contents.getSha224());
  }

  private Version createNewVersionWithContents() {
    final var id = versionIdGenerator.get();
    final var version = new Version(id, file.getId(), contents.getSha224(), now());
    contents().insert(contents);
    versions().insert(version);
    return version;
  }

  private VersionsDao versions() {
    return transaction.attach(VersionsDao.class);
  }

  private ContentsDao contents() {
    return transaction.attach(ContentsDao.class);
  }
}
