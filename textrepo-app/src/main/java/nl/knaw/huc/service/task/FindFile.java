package nl.knaw.huc.service.task;

import static java.util.Objects.requireNonNull;

import java.util.UUID;
import java.util.function.Supplier;
import javax.ws.rs.NotFoundException;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.db.FilesDao;
import org.jdbi.v3.core.Handle;

public class FindFile implements InTransactionProvider<TextRepoFile> {
  private final UUID fileId;

  public FindFile(UUID fileId) {
    this.fileId = requireNonNull(fileId);
  }

  @Override
  public TextRepoFile executeIn(Handle transaction) {
    return transaction.attach(FilesDao.class).find(fileId).orElseThrow(fileNotFound());
  }

  private Supplier<NotFoundException> fileNotFound() {
    return () -> new NotFoundException(String.format("File not found: %s", fileId));
  }

}
