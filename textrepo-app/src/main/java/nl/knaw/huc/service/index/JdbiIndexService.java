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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

/**
 * Handle mutations of indices using configured indexers
 */
public class JdbiIndexService implements IndexService {

  private final List<IndexerClient> indexers;
  private final List<TextRepoElasticClient> indexClients;
  private final Jdbi jdbi;

  public JdbiIndexService(
      List<IndexerClient> indexers,
      List<TextRepoElasticClient> indexClients,
      Jdbi jdbi
  ) {
    this.indexers = indexers;
    this.indexClients = indexClients;
    this.jdbi = jdbi;
  }

  @Override
  public void index(@Nonnull UUID fileId) {
    var found = jdbi
        .onDemand(FilesDao.class)
        .find(fileId)
        .orElseThrow(noSuchFile(fileId));
    index(found);
  }

  @Override
  public void index(@Nonnull TextRepoFile file) {
    var latestContents = getLatestVersionContents(file);
    index(file, latestContents);
  }

  @Override
  public void index(@Nonnull String indexer, @Nonnull TextRepoFile file) {
    var contents = getLatestVersionContents(file);
    var type = getType(file);
    getIndexer(indexer).index(file.getId(), type.getMimetype(), contents);
  }

  @Override
  public void index(@Nonnull TextRepoFile file, String contents) {
    var type = getType(file);
    index(file.getId(), type.getMimetype(), contents);
  }

  @Override
  public void index(@Nonnull UUID file, String mimetype, String contents) {
    indexers.forEach(indexer -> {
      indexer.index(file, mimetype, contents);
    });
  }

  @Override
  public void delete(UUID fileId) {
    indexClients.forEach(indexClient -> indexClient.delete(fileId));
  }

  @Override
  public List<UUID> getAllIds() {
    var result = new HashSet<UUID>();
    indexClients.forEach(indexClient -> result.addAll(indexClient.getAllIds()));
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

  private IndexerClient getIndexer(String name) {
    return indexers
        .stream()
        .filter(i -> i.getConfig().name.equals(name))
        .findFirst()
        .orElseThrow(noSuchIndexer(name));
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

  private Supplier<NotFoundException> noSuchType(Short id) {
    return () -> new NotFoundException(format("No such type: %s", id));
  }

  private Supplier<NotFoundException> noSuchFile(UUID id) {
    return () -> new NotFoundException(format("No such file: %s", id));
  }

}
