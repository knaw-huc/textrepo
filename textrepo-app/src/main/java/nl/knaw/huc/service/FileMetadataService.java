package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

public interface FileMetadataService {
  Map<String, String> getMetadata(UUID fileId);

  void insert(@Nonnull UUID fileId, @Nonnull MetadataEntry entry);

  void upsert(@Nonnull UUID fileId, MetadataEntry entry);

  void delete(UUID fileId, String key);
}
