package nl.knaw.huc.service.version.metadata;

import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import nl.knaw.huc.api.MetadataEntry;

public interface VersionMetadataService {
  Map<String, String> getMetadata(UUID versionId);

  void insert(@Nonnull UUID versionId, @Nonnull MetadataEntry entry);

  void upsert(@Nonnull UUID versionId, MetadataEntry entry);

  void delete(UUID versionId, String key);
}
