package nl.knaw.huc.service.task;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.FileMetadataDao;
import org.jdbi.v3.core.Handle;

import static java.util.Objects.requireNonNull;

public class SetFileProvenance implements ProvidesInTransaction<MetadataEntry> {
  private final TextrepoFile file;
  private final String filename;

  public SetFileProvenance(TextrepoFile file, String filename) {
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
