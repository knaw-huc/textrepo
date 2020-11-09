package nl.knaw.huc.service.version.metadata;

import nl.knaw.huc.api.MetadataEntry;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

public interface VersionMetadataService {
  Map<String, String> getMetadata(UUID versionId);

  void insert(@Nonnull UUID versionId, @Nonnull MetadataEntry entry);

  void upsert(@Nonnull UUID versionId, MetadataEntry entry);

  void delete(UUID versionId, String key);
}
