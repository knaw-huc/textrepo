package nl.knaw.huc.service.task.indexer;

import nl.knaw.huc.service.task.Task;

import java.util.List;
import java.util.UUID;

/**
 * Remove all ES docs with file IDs not present in database
 */
public interface RemoveDeletedFilesFromIndicesTaskBuilder {

  Task<List<UUID>> build();
}
