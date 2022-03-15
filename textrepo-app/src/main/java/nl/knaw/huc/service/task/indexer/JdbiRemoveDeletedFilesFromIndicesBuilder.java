package nl.knaw.huc.service.task.indexer;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.service.index.IndexService;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remove ES docs without file IDs.
 * Removes all ES docs with file IDs not present in database by:
 * - retrieving all ES doc IDs
 * - comparing es IDs to database file IDs
 * - removing all ES docs with an ID not found the file table
 */

public class JdbiRemoveDeletedFilesFromIndicesBuilder
    implements RemoveDeletedFilesFromIndicesTaskBuilder {
  private static final Logger log =
      LoggerFactory.getLogger(JdbiRemoveDeletedFilesFromIndicesBuilder.class);

  private final Jdbi jdbi;
  private final IndexService indexService;

  public JdbiRemoveDeletedFilesFromIndicesBuilder(Jdbi jdbi, IndexService indexService) {
    this.jdbi = requireNonNull(jdbi);
    this.indexService = requireNonNull(indexService);
  }

  @Override
  public Task<List<UUID>> build() {
    return new JdbiRemoveDeletedFilesFromIndicesTask();
  }

  private class JdbiRemoveDeletedFilesFromIndicesTask implements Task<List<UUID>> {

    private JdbiRemoveDeletedFilesFromIndicesTask() {
    }

    @Override
    public List<UUID> run() {
      log.debug("Removing all orphaned docs from indices");
      var esDocIds = indexService.getAllIds();
      log.debug("Got {} ES doc IDs from all indices", esDocIds.size());
      var fileIds = jdbi.onDemand(FilesDao.class).getAll();
      log.debug("Got {} database file IDs", fileIds.size());
      var toDelete = new ArrayList<>(esDocIds);
      toDelete.removeAll(fileIds);
      log.debug("Found {} IDs not present in database", toDelete.size());
      toDelete.forEach(indexService::delete);
      log.debug("Removed orphaned docs");
      return toDelete;
    }
  }

}
