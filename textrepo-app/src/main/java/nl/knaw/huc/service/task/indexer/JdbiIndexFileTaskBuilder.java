package nl.knaw.huc.service.task.indexer;

import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Type;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.db.TypesDao;
import nl.knaw.huc.service.index.IndexService;
import nl.knaw.huc.service.index.IndexerClient;
import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.FindDocumentFileByType;
import nl.knaw.huc.service.task.FindType;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.List.of;
import static java.util.Objects.requireNonNull;

public class JdbiIndexFileTaskBuilder implements IndexFileTaskBuilder {
  private static final Logger log = LoggerFactory.getLogger(JdbiIndexFileTaskBuilder.class);

  private final Jdbi jdbi;
  private final IndexService indexService;

  private String externalId;
  private String typeName;
  private String indexName;

  private long filesAffected = 0;
  private long filesTotal = -1;

  public JdbiIndexFileTaskBuilder(Jdbi jdbi, IndexService indexService) {
    this.jdbi = requireNonNull(jdbi);
    this.indexService = requireNonNull(indexService);
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
        indexService.index(file);
        return format("Indexed file %s", file.getId());
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
        indexService.index(file.getId());
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
    private final Optional<List<String>> mimetypes;
    private final String indexer;

    public JdbiIndexAllFilesByIndexTask(String indexer) {
      this.indexer = indexer;
      this.mimetypes = indexService.getMimetypes(indexer);
    }

    @Override
    public String run() {
      var typesToIndex = getTypesToIndex();

      filesTotal = jdbi.onDemand(FilesDao.class).countByTypes(typesToIndex);
      typesToIndex.forEach(this::indexFilesByType);

      final var msg = format("Total files affected: %d", filesAffected);
      log.info(msg);
      return msg;
    }

    private TypesDao types() {
      return jdbi.onDemand(TypesDao.class);
    }

    private List<Short> getTypesToIndex() {
      var allTypes = types().list();
      List<Type> toIndex;
      if (mimetypes.isEmpty()) {
        toIndex = allTypes;
      } else {
        var supported = mimetypes.get();
        toIndex = allTypes
            .stream()
            .filter(type -> supported.contains(type.getMimetype()))
            .toList();
      }
      return toIndex.stream().map(Type::getId).toList();
    }

    private void indexFilesByType(Short typeId) {
      log.info("Indexing files by type: {}", typeId);
      jdbi.onDemand(FilesDao.class).foreachByType(typeId, file -> indexService.index(indexer, file));
    }

  }
}
