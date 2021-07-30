package nl.knaw.huc.service.task.indexer;

import nl.knaw.huc.service.task.Task;

/**
 * Remove all ES docs with file IDs not present in database
 */
public interface RemoveDeletedFilesFromIndicesTaskBuilder {

  Task<String> build();
}
