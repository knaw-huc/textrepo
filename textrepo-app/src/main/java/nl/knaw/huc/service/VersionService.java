package nl.knaw.huc.service;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Version;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VersionService {

  Optional<Version> findLatestVersion(@Nonnull UUID fileId);

  Version createNewVersion(
      @Nonnull UUID fileId,
      @Nonnull Contents contents,
      @Nonnull LocalDateTime time
  );

  Version createNewVersion(
      @Nonnull TextrepoFile file,
      @Nonnull Contents contents,
      @Nonnull LocalDateTime time
  );

  List<Version> getAll(UUID fileId);

  Version get(UUID id);

  void delete(UUID id);
}
