package nl.knaw.huc.service.task;

import static java.util.Objects.requireNonNull;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.db.FileMetadataDao;
import org.jdbi.v3.core.Handle;

public class SetFileProvenance implements InTransactionProvider<MetadataEntry> {
  private final TextRepoFile file;
  private final String filename;

  public SetFileProvenance(TextRepoFile file, String filename) {
    this.file = requireNonNull(file);
    this.filename = requireNonNull(filename);
  }

  @Override
  public MetadataEntry executeIn(Handle transaction) {
    final var entry = new MetadataEntry("filename", filename);
    transaction.attach(FileMetadataDao.class)
               .upsert(file.getId(), entry);
    return entry;
  }

}
