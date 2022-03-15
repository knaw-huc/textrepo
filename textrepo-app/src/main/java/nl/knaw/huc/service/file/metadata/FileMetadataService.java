package nl.knaw.huc.service.file.metadata;

import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import nl.knaw.huc.api.MetadataEntry;

public interface FileMetadataService {
  Map<String, String> getMetadata(UUID fileId);

  void insert(@Nonnull UUID fileId, @Nonnull MetadataEntry entry);

  void upsert(@Nonnull UUID fileId, MetadataEntry entry);

  void delete(UUID fileId, String key);
}
