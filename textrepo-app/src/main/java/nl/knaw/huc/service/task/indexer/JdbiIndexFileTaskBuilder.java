package nl.knaw.huc.service.task.indexer;

import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.db.TypesDao;
import nl.knaw.huc.service.index.Indexer;
import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.FindDocumentFileByType;
import nl.knaw.huc.service.task.FindType;
import nl.knaw.huc.service.task.GetLatestOptionalFileVersion;
import nl.knaw.huc.service.task.GetVersionContent;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.List.of;
import static java.util.Objects.requireNonNull;

public class JdbiIndexFileTaskBuilder implements IndexFileTaskBuilder {
  private static final Logger log = LoggerFactory.getLogger(JdbiIndexFileTaskBuilder.class);

  private final Jdbi jdbi;
  private final List<Indexer> indexers;

  private String externalId;
  private String typeName;
  private String indexName;

  private long filesAffected = 0;
  private long filesTotal = -1;

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
  public IndexFileTaskBuilder forIndex(String name) {
    this.indexName = name;
    return this;
  }

  @Override
  public IndexFileTaskBuilder withType(String typeName) {
    this.typeName = requireNonNull(typeName);
    return this;
  }

  @Override
  public Task<String> build() {
    if (indexName != null) {
      return new JdbiIndexAllFilesByIndexTask(indexName);
    }
    if (externalId == null) {
      return new JdbiIndexAllFilesTask(typeName);
    }
    return new JdbiIndexFileTask(externalId, typeName);
  }

  /**
   * Index file by externalId and file type name
   */
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
        final var type = new FindType(typeName).executeIn(txn);
        final var file = new FindDocumentFileByType(doc, type).executeIn(txn);
        final var version = new GetLatestOptionalFileVersion(file).executeIn(txn);
        final var contents = getVersionContentsOrEmptyString(txn, version);
        final var results = new ArrayList<String>();
        indexers.forEach((indexer) -> {
          var indexerName = indexer.getClass().getName();
          var indexResult = indexer.index(file, contents).orElse("Ok");
          var result = indexerName + " - " + indexResult;
          results.add(result);
          log.info(result);
        });
        return results.toString();
      });
    }
  }

  /**
   * Index all files with type
   */
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
      final var msg = format("Total files affected: %d", filesAffected);
      log.info(msg);
      return msg;
    }

    private Short resolveType() {
      return types().findByName(typeName).orElseThrow(noSuchType(typeName));
    }

    private TypesDao types() {
      return jdbi.onDemand(TypesDao.class);
    }

    private Supplier<NotFoundException> noSuchType(String typeName) {
      return () -> new NotFoundException(format("No such type: %s", typeName));
    }

    private void indexFilesByType(Short typeId) {
      filesTotal += jdbi.onDemand(FilesDao.class).countByTypes(of(typeId));
      jdbi.onDemand(FilesDao.class).foreachByType(typeId, this::indexFile);
    }

    private void indexFile(TextRepoFile file) {
      log.debug("Indexing file: {}", file.getId());
      jdbi.useTransaction(txn -> {
        var version = new GetLatestOptionalFileVersion(file).executeIn(txn);
        var contents = getVersionContentsOrEmptyString(txn, version);
        indexers.forEach((indexer) -> {
          var result = indexer.index(file, contents);
          result.ifPresent((str) -> log.warn(indexer.getClass().getName() + " - " + str));
        });
        filesAffected++;
        log.info("Indexed file {} ({} of estimated {})", file.getId(), filesAffected, filesTotal);
      });
    }
  }

  /**
   * Index all files relevant to indexer
   */
  public class JdbiIndexAllFilesByIndexTask implements Task<String> {

    private final Logger log = LoggerFactory.getLogger(JdbiIndexAllFilesTask.class);
    private final Indexer indexer;

    public JdbiIndexAllFilesByIndexTask(String indexName) {
      this.indexer = indexers
          .stream()
          .filter(i -> i.getConfig().name.equals(indexName))
          .findFirst()
          .orElseThrow(noSuchIndexer(indexName));
    }

    @Override
    public String run() {
      var typesToIndex = new ArrayList<Short>();

      indexer.getConfig().mimetypes
          .forEach(m -> {
            var type = types()
                .findByMimetype(m);
            type.ifPresentOrElse(
                typesToIndex::add,
                () -> log.warn("No such mimetype: {}", m)
            );
          });

      filesTotal = jdbi.onDemand(FilesDao.class).countByTypes(typesToIndex);
      typesToIndex.forEach(this::indexFilesByType);

      final var msg = format("Total files affected: %d", filesAffected);
      log.info(msg);
      return msg;
    }

    private TypesDao types() {
      return jdbi.onDemand(TypesDao.class);
    }

    private Supplier<NotFoundException> noSuchIndexer(String name) {
      return () -> new NotFoundException(format("No such indexer: %s", name));
    }

    private void indexFilesByType(Short typeId) {
      log.info("Indexing files by type: {}", typeId);
      jdbi.onDemand(FilesDao.class).foreachByType(typeId, this::indexFile);
    }

    private void indexFile(TextRepoFile file) {
      log.debug("Indexing file: {}", file.getId());
      jdbi.useTransaction(txn -> {
        var version = new GetLatestOptionalFileVersion(file).executeIn(txn);
        var contents = getVersionContentsOrEmptyString(txn, version);
        var result = indexer.index(file, contents);
        result.ifPresent((str) -> log.warn(indexer.getClass().getName() + " - " + str));
        filesAffected++;
        log.info("Indexed file {} ({} of estimated {})", file.getId(), filesAffected, filesTotal);
      });
    }

  }

  /**
   * Retrieve version contents, or return an empty string when no version present
   */
  private String getVersionContentsOrEmptyString(Handle txn, Optional<Version> version) {
    String contents;
    if (version.isEmpty()) {
      log.info("No version found, using empty string");
      contents = "";
    } else {
      contents = new GetVersionContent(version.get()).executeIn(txn).asUtf8String();
    }
    return contents;
  }
}
