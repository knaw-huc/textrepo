package nl.knaw.huc.resources.task;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.ContentsDao;
import nl.knaw.huc.db.VersionDao;
import org.jdbi.v3.core.Jdbi;

import java.util.function.Function;

import static java.time.LocalDateTime.now;

class GetOrCreateVersion implements Function<TextrepoFile, Version> {
  private final Jdbi jdbi;
  private final Contents contents;

  GetOrCreateVersion(Jdbi jdbi, Contents contents) {
    this.jdbi = jdbi;
    this.contents = contents;
  }

  @Override
  public Version apply(TextrepoFile file) {
    return versions().findLatestByFileId(file.getId())
                     .filter(v -> v.getContentsSha().equals(contents.getSha224()))
                     .orElseGet(() -> createVersion(file));
  }

  private Version createVersion(TextrepoFile file) {
    contents().insert(contents);
    return new Version(file.getId(), now(), contents.getSha224());
  }

  private VersionDao versions() {
    return jdbi.onDemand(VersionDao.class);
  }

  private ContentsDao contents() {
    return jdbi.onDemand(ContentsDao.class);
  }
}
