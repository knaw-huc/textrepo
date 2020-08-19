package nl.knaw.huc.service.file;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Version;

import java.util.UUID;

public interface FileService {
  Version addFile(Contents contents, TextRepoFile file);

  TextRepoFile insert(UUID docId, TextRepoFile textRepoFile);

  TextRepoFile get(UUID fileId);

  UUID getDocumentId(UUID fileId);

  TextRepoFile upsert(UUID docId, TextRepoFile textRepoFile);

  void delete(UUID fileId);
}
