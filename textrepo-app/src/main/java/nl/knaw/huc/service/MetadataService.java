package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetadataService {
  List<DocumentService.KeyValue> getMetadata(UUID documentId);

  void addMetadata(@Nonnull UUID documentId, @Nonnull List<DocumentService.KeyValue> metadata);

  void insert(@Nonnull MetadataEntry entry);

  void bulkInsert(@Nonnull List<MetadataEntry> entries);

  List<MetadataEntry> find(@Nonnull UUID documentId);

}
