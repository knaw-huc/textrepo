package nl.knaw.huc.resources.task.jdbi;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.ContentsDao;
import nl.knaw.huc.db.VersionDao;
import org.jdbi.v3.core.Jdbi;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.time.LocalDateTime.now;

class SetCurrentFileContents implements Function<TextrepoFile, Version> {
  private final Jdbi jdbi;
  private final Contents contents;

  SetCurrentFileContents(Jdbi jdbi, Contents contents) {
    this.jdbi = jdbi;
    this.contents = contents;
  }

  @Override
  public Version apply(TextrepoFile file) {
    return versions().findLatestByFileId(file.getId())
                     .filter(hasIdenticalContents())
                     .orElseGet(createNewVersionWithContents(file));
  }

  private Predicate<Version> hasIdenticalContents() {
    return candidate -> candidate.getContentsSha().equals(contents.getSha224());
  }

  private Supplier<Version> createNewVersionWithContents(TextrepoFile file) {
    return () -> {
      contents().insert(contents);
      return new Version(file.getId(), now(), contents.getSha224());
    };
  }

  private VersionDao versions() {
    return jdbi.onDemand(VersionDao.class);
  }

  private ContentsDao contents() {
    return jdbi.onDemand(ContentsDao.class);
  }
}
