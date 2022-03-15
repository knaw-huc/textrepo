package nl.knaw.huc.service.version;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.annotation.Nonnull;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.core.Version;

public interface VersionService {

  Version createNewVersion(
      @Nonnull UUID fileId,
      @Nonnull Contents contents
  );

  Page<Version> getAll(UUID fileId, PageParams pageParams, LocalDateTime createdAfter);

  Version get(UUID id);

  void delete(UUID id);
}
