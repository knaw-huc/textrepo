package nl.knaw.huc.service.index;

import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Type;
import nl.knaw.huc.db.ContentsDao;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.db.TypesDao;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

/**
 * Handle index mutations using configured indexers and indices
 */
public class JdbiIndexService implements IndexService {

  private static final Logger log = LoggerFactory.getLogger(JdbiIndexService.class);
  private final List<IndexerClient> indexers;
  private final List<EsIndexClient> indices;
  private final Jdbi jdbi;

  public JdbiIndexService(
      List<IndexerClient> indexers,
      List<EsIndexClient> indices,
      Jdbi jdbi
  ) {
    this.indexers = indexers;
    this.indices = indices;
    this.jdbi = jdbi;
    createIndices();
  }

  private void createIndices() {
    for (var indexer : indexers) {
      var index = getIndex(indexer.getConfig().elasticsearch.index);
      var indexName = index.getConfig().index;
      log.info("Creating index {}", indexName);
      final String mapping;
      if (indexer instanceof IndexerWithMappingClient) {
        mapping = ((IndexerWithMappingClient) indexer)
            .getMapping()
            .orElseThrow(noSuchMapping(indexName));
        index.createIndex(mapping);
      } else {
        throw new NotSupportedException("All indexers should have a mapping endpoint");
      }
    }
  }

  @Override
  public void index(@Nonnull UUID fileId) {
    var found = jdbi.onDemand(FilesDao.class).find(fileId).orElseThrow(noSuchFile(fileId));
    index(found);
  }

  @Override
  public void index(@Nonnull TextRepoFile file) {
    var latestContents = getLatestVersionContents(file);
    index(file, latestContents);
  }

  @Override
  public void index(@Nonnull String indexerName, @Nonnull TextRepoFile file) {
    var contents = getLatestVersionContents(file);
    var type = getType(file);
    createAndUpsertEsDoc(indexerName, file.getId(), contents, type.getMimetype());
  }

  @Override
  public void index(@Nonnull TextRepoFile file, String contents) {
    var type = getType(file);
    index(file.getId(), type.getMimetype(), contents);
  }

  @Override
  public void index(@Nonnull UUID file, String mimetype, String contents) {
    indexers.forEach(indexer -> {
      createAndUpsertEsDoc(indexer.getConfig().name, file, contents, mimetype);
    });
  }

  private void createAndUpsertEsDoc(String indexerName, UUID file, String contents, String mimetype) {
    var indexer = getIndexer(indexerName);
    var esDoc = indexer.fields(file, mimetype, contents);
    if (esDoc.isPresent()) {
      var indexName = indexer.getConfig().elasticsearch.index;
      getIndex(indexName).upsert(file, esDoc.get());
    } else {
      log.info(format("Not indexing file %s: indexer %s returned nothing", file, indexerName));
    }
  }

  @Override
  public void delete(UUID fileId) {
    indices.forEach(indexClient -> indexClient.delete(fileId));
  }

  @Override
  public List<UUID> getAllIds() {
    var result = new HashSet<UUID>();
    indices.forEach(indexClient -> result.addAll(indexClient.getAllIds()));
    return result.stream().toList();
  }

  private Type getType(@Nonnull TextRepoFile file) {
    return jdbi
        .onDemand(TypesDao.class)
        .getById(file.getTypeId())
        .orElseThrow(noSuchType(file.getTypeId()));
  }

  @Override
  public Optional<List<String>> getMimetypes(String indexer) {
    return getIndexer(indexer).getMimetypes();
  }

  private IndexerClient getIndexer(String indexerName) {
    return indexers
        .stream()
        .filter(i -> i.getConfig().name.equals(indexerName))
        .findFirst()
        .orElseThrow(noSuchIndexer(indexerName));
  }

  private EsIndexClient getIndex(String indexName) {
    return indices
        .stream()
        .filter(ic -> ic.getConfig().index.equals(indexName))
        .findFirst()
        .orElseThrow(noSuchIndex(indexName));
  }

  private String getLatestVersionContents(TextRepoFile file) {
    var latestVersion = jdbi
        .onDemand(VersionsDao.class)
        .findLatestByFileId(file.getId());
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
    return latestContents;
  }

  private Supplier<NotFoundException> noSuchIndexer(String name) {
    return () -> new NotFoundException(format("No such indexer: %s", name));
  }

  private Supplier<NotFoundException> noSuchIndex(String name) {
    return () -> new NotFoundException(format("Could not find index with name: %s", name));
  }

  private Supplier<NotFoundException> noSuchType(Short id) {
    return () -> new NotFoundException(format("No such type: %s", id));
  }

  private Supplier<NotFoundException> noSuchFile(UUID id) {
    return () -> new NotFoundException(format("No such file: %s", id));
  }

  private Supplier<NotFoundException> noSuchMapping(String index) {
    return () -> new NotFoundException(format("Cannot create index, no mapping found for: %s", index));
  }

}
