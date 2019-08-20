package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetadataService {
  void insert(@Nonnull MetadataEntry entry);

  void bulkInsert(@Nonnull List<MetadataEntry> entries);

  Optional<MetadataEntry> find(@Nonnull UUID documentId, @Nonnull String key);
}
