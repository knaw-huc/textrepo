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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Handle mutations of configured indexers and their indices
 */
public class JdbiIndexService implements IndexService {

  private final List<Indexer> indexers;
  private final List<TextRepoElasticClient> indexClients;
  private final Jdbi jdbi;
  private static final Logger log = LoggerFactory.getLogger(JdbiIndexService.class);
  private final Map<Short, List<Indexer>> indexersByType;

  public JdbiIndexService(
      List<Indexer> indexers,
      List<TextRepoElasticClient> indexClients,
      Jdbi jdbi
  ) {
    this.indexers = indexers;
    this.indexClients = indexClients;
    this.jdbi = jdbi;
    indexersByType = mapIndexersToTypes(indexers, jdbi);
  }

  @Override
  public void index(@Nonnull UUID fileId) {
    var found = jdbi.onDemand(FilesDao.class).find(fileId);
    found.ifPresentOrElse(
        (file) -> {
          var latestContents = getLatestVersionContents(file);
          indexers.forEach(indexer -> indexer.index(file, latestContents));
        },
        () -> {
          throw new NotFoundException(format("Could not find file by id %s", fileId));
        });
  }

  @Override
  public void index(@Nonnull TextRepoFile file) {
    var latestContents = getLatestVersionContents(file);
    indexers.forEach(indexer -> indexer.index(file, latestContents));
  }

  @Override
  public void index(@Nonnull TextRepoFile file, String contents) {
    indexersByType.get(file.getTypeId()).forEach(indexer -> {
      indexer.index(file, contents);
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

  @Override
  public Optional<Indexer> getIndexer(String name) {
    return indexers.stream()
        .filter(i -> i.getConfig().name.equals(name))
        .findFirst();
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

  private Map<Short, List<Indexer>> mapIndexersToTypes(List<Indexer> indexers, Jdbi jdbi) {
    var types = jdbi
        .onDemand(TypesDao.class)
        .list();
    return types.stream().collect(toMap(Type::getId, (t) -> indexers
        .stream()
        .filter(
            indexer -> indexer.getMimetypes().isEmpty() || indexer.getMimetypes().get().contains(t.getMimetype()))
        .collect(toList())
    ));
  }


}
