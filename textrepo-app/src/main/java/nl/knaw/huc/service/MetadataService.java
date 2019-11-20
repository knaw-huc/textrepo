package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public interface MetadataService {
  Map<String, String> getMetadata(UUID fileId);

  void addMetadata(@Nonnull UUID fileId, @Nonnull Map<String, String> metadata);

  void insert(@Nonnull UUID fileId, @Nonnull MetadataEntry entry);

  void update(@Nonnull UUID fileId, MetadataEntry entry);

  Iterator<MetadataEntry> find(@Nonnull UUID fileId);
}
