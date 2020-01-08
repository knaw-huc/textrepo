package nl.knaw.huc.service;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.Version;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface FileService {
  UUID createFile(String type);

  Version createVersionWithFilenameMetadata(UUID fileId, byte[] content, String filename);

  Version addFile(Contents contents, UUID fileId, String filename);

  Version getLatestVersion(@Nonnull UUID fileId);
}
