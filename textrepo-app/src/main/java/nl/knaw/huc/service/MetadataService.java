package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MetadataService {
  Map<String, String> getMetadata(UUID documentId);

  void addMetadata(@Nonnull UUID documentId, @Nonnull Map<String, String> metadata);

  void insert(@Nonnull MetadataEntry entry);

  void bulkInsert(@Nonnull Iterator<MetadataEntry> entries);

  void update(MetadataEntry entry);

  Iterator<MetadataEntry> find(@Nonnull UUID documentId);
}
