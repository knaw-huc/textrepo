package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

public interface VersionService {

  Optional<Version> findLatestVersion(@Nonnull UUID documentId);

  Version insertNewVersion(@Nonnull UUID documentId, @Nonnull TextRepoFile file, String filename);
}
