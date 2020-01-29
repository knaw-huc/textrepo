package nl.knaw.huc.resources.task.jdbi;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.FileDao;
import org.jdbi.v3.core.Handle;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

class HaveFileForDocumentByType implements Function<Document, TextrepoFile> {
  private final Handle transaction;
  private final Supplier<UUID> idGenerator;
  private final short typeId;

  private FileDao fileDao;
  private DocumentFilesDao documentFilesDao;

  HaveFileForDocumentByType(Handle transaction, Supplier<UUID> idGenerator, short typeId) {
    this.transaction = transaction;
    this.idGenerator = idGenerator;
    this.typeId = typeId;
  }

  @Override
  public TextrepoFile apply(Document doc) {
    documentFilesDao = transaction.attach(DocumentFilesDao.class);
    fileDao = transaction.attach(FileDao.class);
    return findFileForDocument(doc).orElseGet(createNewFileForDocument(doc));
  }

  private Optional<TextrepoFile> findFileForDocument(Document doc) {
    return documentFilesDao.findFile(doc.getId(), typeId);
  }

  private Supplier<TextrepoFile> createNewFileForDocument(Document doc) {
    return () -> {
      final var file = new TextrepoFile(idGenerator.get(), typeId);
      fileDao.create(file.getId(), file.getTypeId()); // TODO: implement and use FileDao.save(TextrepoFile file)
      documentFilesDao.insert(doc.getId(), file.getId());
      return file;
    };
  }
}
