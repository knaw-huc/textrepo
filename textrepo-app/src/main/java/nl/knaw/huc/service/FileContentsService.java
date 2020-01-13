package nl.knaw.huc.service;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.Version;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface FileContentsService {
  Contents getLatestFileContents(UUID fileId);

  Version replaceFileContents(
      @Nonnull UUID fileId,
      @Nonnull Contents contents,
      @Nonnull String filename
  );
}
