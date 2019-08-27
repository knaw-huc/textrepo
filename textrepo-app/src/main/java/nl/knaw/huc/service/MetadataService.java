package nl.knaw.huc.service;

import nl.knaw.huc.api.KeyValue;
import nl.knaw.huc.api.MetadataEntry;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MetadataService {
  Map<String, String> getMetadata(UUID documentId);

  void addMetadata(@Nonnull UUID documentId, @Nonnull List<KeyValue> metadata);

  void insert(@Nonnull MetadataEntry entry);

  void bulkInsert(@Nonnull Iterator<MetadataEntry> entries);

  void update(MetadataEntry entry);

  List<MetadataEntry> find(@Nonnull UUID documentId);
}
