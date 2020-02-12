package nl.knaw.huc.service.task;

import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.FilesDao;
import org.jdbi.v3.core.Handle;

import javax.ws.rs.NotFoundException;
import java.util.UUID;
import java.util.function.Supplier;

public class FindFile implements ProvidesInTransaction<TextrepoFile> {
  private final UUID fileId;

  public FindFile(UUID fileId) {
    this.fileId = fileId;
  }

  @Override
  public TextrepoFile executeIn(Handle transaction) {
    return transaction.attach(FilesDao.class).find(fileId).orElseThrow(fileNotFound());
  }

  private Supplier<NotFoundException> fileNotFound() {
    return () -> new NotFoundException(String.format("File not found: %s", fileId));
  }

}