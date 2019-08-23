package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.KeyValue;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public interface MetadataService {
  List<KeyValue> getMetadata(UUID documentId);

  void addMetadata(@Nonnull UUID documentId, @Nonnull List<KeyValue> metadata);

  void insert(@Nonnull MetadataEntry entry);

  void bulkInsert(@Nonnull List<MetadataEntry> entries);

  void update(MetadataEntry entry);

  List<MetadataEntry> find(@Nonnull UUID documentId);
}
