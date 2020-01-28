package nl.knaw.huc.resources.task.jdbi;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.FileDao;
import org.jdbi.v3.core.Jdbi;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

class GetOrCreateFile implements Function<Document, TextrepoFile> {
  private final Jdbi jdbi;
  private final Supplier<UUID> idGenerator;
  private final short typeId;

  GetOrCreateFile(Jdbi jdbi, Supplier<UUID> idGenerator, short typeId) {
    this.jdbi = jdbi;
    this.idGenerator = idGenerator;
    this.typeId = typeId;
  }

  @Override
  public TextrepoFile apply(Document doc) {
    return df().findFile(doc.getId(), typeId)
               .orElseGet(() -> createFile(typeId));
  }

  private TextrepoFile createFile(short typeId) {
    final var fileId = idGenerator.get();
    files().create(fileId, typeId);
    return new TextrepoFile(fileId, typeId);
  }

  private DocumentFilesDao df() {
    return jdbi.onDemand(DocumentFilesDao.class);
  }

  private FileDao files() {
    return jdbi.onDemand(FileDao.class);
  }
}
