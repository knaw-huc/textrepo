package nl.knaw.huc.service;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Version;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface FileService {
  TextrepoFile createFile(String type);

  Version createVersionWithFilenameMetadata(TextrepoFile file, byte[] content, String filename);

  Version addFile(Contents contents, TextrepoFile file, String filename);

  Version getLatestVersion(@Nonnull UUID fileId);
}
