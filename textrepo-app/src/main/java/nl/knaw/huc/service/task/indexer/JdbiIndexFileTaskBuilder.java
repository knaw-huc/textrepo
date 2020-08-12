package nl.knaw.huc.service.task.indexer;

import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.db.TypesDao;
import nl.knaw.huc.service.index.Indexer;
import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.FindDocumentFileByType;
import nl.knaw.huc.service.task.GetLatestFileContents;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class JdbiIndexFileTaskBuilder implements IndexFileTaskBuilder {
  private static final Logger log = LoggerFactory.getLogger(JdbiIndexFileTaskBuilder.class);

  private final Jdbi jdbi;
  private final List<Indexer> indexers;

  private String externalId;
  private String typeName;

  public JdbiIndexFileTaskBuilder(Jdbi jdbi, List<Indexer> indexers) {
    this.jdbi = requireNonNull(jdbi);
    this.indexers = requireNonNull(indexers);
  }

  @Override
  public IndexFileTaskBuilder forExternalId(String externalId) {
    this.externalId = requireNonNull(externalId);
    return this;
  }

  @Override
  public IndexFileTaskBuilder withType(String typeName) {
    this.typeName = requireNonNull(typeName);
    return this;
  }

  @Override
  public Task<String> build() {
    if (externalId == null) {
      return new JdbiIndexAllFilesTask(typeName);
    }
    return new JdbiIndexFileTask(externalId, typeName);
  }

  private class JdbiIndexFileTask implements Task<String> {
    private final String externalId;
    private final String typeName;

    private JdbiIndexFileTask(String externalId, String typeName) {
      this.externalId = externalId;
      this.typeName = typeName;
    }

    @Override
    public String run() {
      return jdbi.inTransaction(txn -> {
        final var doc = new FindDocumentByExternalId(externalId).executeIn(txn);
        final var file = new FindDocumentFileByType(doc, typeName).executeIn(txn);
        final var contents = new GetLatestFileContents(file).executeIn(txn);
        final var results = new ArrayList<String>();
        indexers.forEach((indexer) -> {
          var result = indexer.getClass().getName() + " - " + indexer.index(file, contents.asUtf8String()).orElse("Ok");
          results.add(result);
          log.info(result);
        });
        return results.toString();
      });
    }
  }

  private class JdbiIndexAllFilesTask implements Task<String> {
    private final Logger log = LoggerFactory.getLogger(JdbiIndexAllFilesTask.class);

    private final String typeName;

    private int filesAffected = 0;

    private JdbiIndexAllFilesTask(String typeName) {
      this.typeName = typeName;
    }

    @Override
    public String run() {
      indexFilesByType(resolveType());
      final var msg = String.format("Total files affected: %d", filesAffected);
      log.info(msg);
      return msg;
    }

    private Short resolveType() {
      return types().find(typeName).orElseThrow(noSuchType(typeName));
    }

    private TypesDao types() {
      return jdbi.onDemand(TypesDao.class);
    }

    private Supplier<NotFoundException> noSuchType(String typeName) {
      return () -> new NotFoundException(String.format("No such type: %s", typeName));
    }

    private void indexFilesByType(Short typeId) {
      jdbi.onDemand(FilesDao.class).foreachByType(typeId, this::indexFile);
    }

    private void indexFile(TextRepoFile file) {
      log.debug("Indexing file: {}", file.getId());
      jdbi.useTransaction(txn -> {
        final var contents = new GetLatestFileContents(file).executeIn(txn);

        indexers.forEach((indexer) -> {
          var result = indexer.index(file, contents.asUtf8String());
          result.ifPresent((str) -> log.warn(indexer.getClass().getName() + " - " + str));
        });
        filesAffected++;
      });
    }
  }
}
