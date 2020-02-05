package nl.knaw.huc.service;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Version;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface FileService {
  TextrepoFile createFile(String type, String filename);

  Version createVersion(TextrepoFile file, byte[] content);

  Version addFile(Contents contents, TextrepoFile file);

  Version getLatestVersion(@Nonnull UUID fileId);

  TextrepoFile create(UUID docId, TextrepoFile textrepoFile);

  TextrepoFile get(UUID fileId);

  UUID getDocumentId(UUID fileId);

  TextrepoFile upsert(UUID docId, TextrepoFile textrepoFile);

  void delete(UUID fileId);
}
