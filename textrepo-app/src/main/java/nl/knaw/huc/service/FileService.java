package nl.knaw.huc.service;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Version;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface FileService {
  TextRepoFile createFile(String type, String filename);

  Version createVersion(TextRepoFile file, byte[] contents);

  Version addFile(Contents contents, TextRepoFile file);

  Version getLatestVersion(@Nonnull UUID fileId);

  TextRepoFile insert(UUID docId, TextRepoFile textRepoFile);

  TextRepoFile get(UUID fileId);

  UUID getDocumentId(UUID fileId);

  TextRepoFile upsert(UUID docId, TextRepoFile textRepoFile);

  void delete(UUID fileId);
}
