package nl.knaw.huc.service.task;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.UUID;
import nl.knaw.huc.db.FileMetadataDao;
import org.jdbi.v3.core.Handle;

public class GetFileMetadata implements InTransactionProvider<Map<String, String>> {
  private final UUID fileId;

  public GetFileMetadata(UUID fileId) {
    this.fileId = requireNonNull(fileId);
  }

  @Override
  public Map<String, String> executeIn(Handle transaction) {
    var fileMetadataDao = transaction.attach(FileMetadataDao.class);
    return fileMetadataDao.getMetadataByFileId(fileId);
  }

}
