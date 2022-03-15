package nl.knaw.huc.service.task;

import static java.time.LocalDateTime.now;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.ContentsDao;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Handle;

public class SetFileContents implements InTransactionProvider<Version> {

  private final Supplier<UUID> idGenerator;

  private final TextRepoFile file;
  private final Contents contents;
  private final boolean asLatestVersion;

  private Handle transaction;

  public SetFileContents(Supplier<UUID> idGenerator, TextRepoFile file, Contents contents,
                         boolean asLatestVersion) {
    this.idGenerator = requireNonNull(idGenerator);
    this.file = requireNonNull(file);
    this.contents = requireNonNull(contents);
    this.asLatestVersion = asLatestVersion;
  }

  @Override
  public Version executeIn(Handle transaction) {
    this.transaction = requireNonNull(transaction);
    if (asLatestVersion) {
      return latestVersionIfIdentical().orElseGet(this::createNewVersionWithContents);
    }
    return anyVersionIfIdentical().orElseGet(this::createNewVersionWithContents);
  }

  private Optional<Version> latestVersionIfIdentical() {
    return versions().findLatestByFileId(file.getId())
                     .filter(this::hasIdenticalContents);
  }

  private Optional<Version> anyVersionIfIdentical() {
    return versions().findByFileId(file.getId()).stream()
                     .filter(this::hasIdenticalContents)
                     .findFirst();
  }

  private boolean hasIdenticalContents(Version candidate) {
    return candidate.getContentsSha().equals(contents.getSha224());
  }

  private Version createNewVersionWithContents() {
    final var id = idGenerator.get();
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
