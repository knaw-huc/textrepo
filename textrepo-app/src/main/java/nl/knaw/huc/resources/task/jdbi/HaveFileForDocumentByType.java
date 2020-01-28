package nl.knaw.huc.resources.task.jdbi;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.FileDao;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

class HaveFileForDocumentByType implements Function<Document, TextrepoFile> {
  private final Jdbi jdbi;
  private final Supplier<UUID> idGenerator;
  private final short typeId;

  HaveFileForDocumentByType(Jdbi jdbi, Supplier<UUID> idGenerator, short typeId) {
    this.jdbi = jdbi;
    this.idGenerator = idGenerator;
    this.typeId = typeId;
  }

  @Override
  public TextrepoFile apply(Document doc) {
    return findFileForDocument(doc).orElseGet(createNewFileForDocument(doc));
  }

  private Optional<TextrepoFile> findFileForDocument(Document doc) {
    return df().findFile(doc.getId(), typeId);
  }

  private Supplier<TextrepoFile> createNewFileForDocument(Document doc) {
    return () -> {
      final var file = new TextrepoFile(idGenerator.get(), typeId);
      files().create(file.getId(), file.getTypeId()); // TODO: implement and use FileDao.save(TextrepoFile file)
      df().insert(doc.getId(), file.getId());
      return file;
    };
  }

  private DocumentFilesDao df() {
    return jdbi.onDemand(DocumentFilesDao.class);
  }

  private FileDao files() {
    return jdbi.onDemand(FileDao.class);
  }
}
