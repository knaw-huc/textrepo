package nl.knaw.huc.service.task;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.FileMetadataDao;
import org.jdbi.v3.core.Handle;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class SetFileProvenance implements Function<Handle, MetadataEntry> {
  private final TextrepoFile file;
  private final String filename;

  private Handle transaction;

  public SetFileProvenance(TextrepoFile file, String filename) {
    this.file = requireNonNull(file);
    this.filename = requireNonNull(filename);
  }

  @Override
  public MetadataEntry apply(Handle transaction) {
    this.transaction = requireNonNull(transaction);
    final var entry = new MetadataEntry("filename", this.filename);
    metadata().upsertFileMetadata(file, entry);
    return entry;
  }

  private FileMetadataDao metadata() {
    return transaction.attach(FileMetadataDao.class);
  }
}
