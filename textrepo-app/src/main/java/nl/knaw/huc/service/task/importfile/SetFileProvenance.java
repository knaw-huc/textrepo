package nl.knaw.huc.service.task.importfile;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.FileMetadataDao;
import org.jdbi.v3.core.Handle;

import java.util.function.Function;

class SetFileProvenance implements Function<TextrepoFile, TextrepoFile> {
  private final Handle transaction;
  private final String filename;

  SetFileProvenance(Handle transaction, String filename) {
    this.transaction = transaction;
    this.filename = filename;
  }

  @Override
  public TextrepoFile apply(TextrepoFile file) {
    metadata().upsertFileMetadata(file, new MetadataEntry("filename", filename));
    return file;
  }

  private FileMetadataDao metadata() {
    return transaction.attach(FileMetadataDao.class);
  }
}
