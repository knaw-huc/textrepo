package nl.knaw.huc.service.task.indexer;

import java.util.List;
import java.util.UUID;
import nl.knaw.huc.service.task.Task;

/**
 * Remove all ES docs with file IDs not present in database.
 */
public interface RemoveDeletedFilesFromIndicesTaskBuilder {

  Task<List<UUID>> build();
}
