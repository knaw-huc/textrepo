package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface VersionService {
  Version getLatestVersion(@Nonnull UUID documentId);

  Version insertNewVersion(@Nonnull UUID documentId, @Nonnull TextRepoFile file);

  Version replace(@Nonnull UUID documentId, @Nonnull TextRepoFile file);
}
