package nl.knaw.huc.service.index;

import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.db.ContentsDao;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

import static java.lang.String.format;

public class JdbiIndexService implements IndexService {

  private final List<Indexer> indexers;
  private final Jdbi jdbi;
  private static final Logger log = LoggerFactory.getLogger(MappedIndexer.class);

  public JdbiIndexService(
      List<Indexer> indexers,
      Jdbi jdbi
  ) {
    this.indexers = indexers;
    this.jdbi = jdbi;
  }

  @Override
  public void index(@Nonnull TextRepoFile file) {
    var latestVersion = jdbi
        .onDemand(VersionsDao.class)
        .findLatestByFileId(file.getId());
    log.info(format("Indexing file %s", file.getId()));
    String latestContents;
    if (latestVersion.isEmpty()) {
      latestContents = "";
    } else {
      latestContents = jdbi
          .onDemand(ContentsDao.class)
          .findBySha224(latestVersion.get().getContentsSha())
          .orElseThrow(() -> new IllegalStateException(""))
          .asUtf8String();
    }
    indexers.forEach(indexer ->
        indexer.index(file, latestContents)
    );
  }
}
