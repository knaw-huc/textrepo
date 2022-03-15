package nl.knaw.huc.service.file;

import java.util.UUID;
import nl.knaw.huc.core.TextRepoFile;

public interface FileService {
  TextRepoFile insert(UUID docId, TextRepoFile textRepoFile);

  TextRepoFile get(UUID fileId);

  UUID getDocumentId(UUID fileId);

  TextRepoFile upsert(UUID docId, TextRepoFile textRepoFile);

  void delete(UUID fileId);
}
