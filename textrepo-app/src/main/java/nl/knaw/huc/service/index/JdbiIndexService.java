package nl.knaw.huc.service.index;

import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.db.ContentsDao;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.db.VersionsDao;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.elasticsearch.client.RequestOptions.DEFAULT;

public class JdbiIndexService implements IndexService {

  private final List<Indexer> indexers;
  private final List<TextRepoElasticClient> indexClients;
  private final Jdbi jdbi;
  private static final Logger log = LoggerFactory.getLogger(IndexerWithMapping.class);

  public JdbiIndexService(
      List<Indexer> indexers,
      List<TextRepoElasticClient> indexClients,
      Jdbi jdbi
  ) {
    this.indexers = indexers;
    this.indexClients = indexClients;
    this.jdbi = jdbi;
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
    indexers.forEach(i -> i.index(file, latestContents));
  }

  @Override
  public void index(@Nonnull TextRepoFile file, String contents) {
    indexers.forEach(i -> i.index(file, contents));
  }

  @Override
  public void delete(UUID fileId) {
    indexClients.forEach(indexClient -> deleteInIndex(fileId, indexClient));
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

  private void deleteInIndex(@Nonnull UUID fileId, TextRepoElasticClient client) {
    var index = client.getConfig().index;
    log.info(format("Deleting file %s from index %s", fileId, client.getClient()));
    DeleteResponse response;
    var deleteRequest = new DeleteRequest();
    deleteRequest.index(index);
    deleteRequest.id(fileId.toString());
    try {
      response = client.getClient().delete(deleteRequest, DEFAULT);
    } catch (Exception ex) {
      throw new WebApplicationException(format("Could not delete file %s in index %s", fileId, index), ex);
    }
    var status = response.status().getStatus();
    final String msg;
    if (status == 200) {
      msg = format("Successfully deleted file %s from index %s", fileId, index);
    } else if (status == 404) {
      msg = format("File %s not found in index %s", fileId, index);
    } else {
      throw new WebApplicationException(format("Could not delete file %s from index %s", fileId, index));
    }
    log.info(msg);
  }



}
