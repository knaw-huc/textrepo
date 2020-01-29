package nl.knaw.huc.resources.task.jdbi;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.MetadataDao;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.util.function.Function;

class SetFilenameMetadata implements Function<TextrepoFile, TextrepoFile> {
  private final Handle transaction;
  private final String filename;

  SetFilenameMetadata(Handle transaction, String filename) {
    this.transaction = transaction;
    this.filename = filename;
  }

  @Override
  public TextrepoFile apply(TextrepoFile file) {
    metadata().upsertFileMetadata(file, new MetadataEntry("filename", filename));
    return file;
  }

  private MetadataDao metadata() {
    return transaction.attach(MetadataDao.class);
  }
}
