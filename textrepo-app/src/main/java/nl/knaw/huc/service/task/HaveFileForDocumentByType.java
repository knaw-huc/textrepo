package nl.knaw.huc.service.task;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import javax.ws.rs.NotFoundException;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.db.TypesDao;
import org.jdbi.v3.core.Handle;

public class HaveFileForDocumentByType implements InTransactionProvider<TextRepoFile> {
  private final Supplier<UUID> idGenerator;
  private final Document doc;
  private final String typeName;

  private Handle transaction;

  public HaveFileForDocumentByType(Supplier<UUID> idGenerator, Document doc, String typeName) {
    this.idGenerator = requireNonNull(idGenerator);
    this.doc = requireNonNull(doc);
    this.typeName = requireNonNull(typeName);
  }

  @Override
  public TextRepoFile executeIn(Handle transaction) {
    this.transaction = requireNonNull(transaction);
    final var typeId = getTypeId();
    return findFileForDocument(doc, typeId).orElseGet(createNewFileForDocument(doc, typeId));
  }

  private Short getTypeId() {
    return types().findByName(typeName).orElseThrow(illegalType(typeName));
  }

  private Supplier<NotFoundException> illegalType(String name) {
    return () -> new NotFoundException(String.format("Illegal type: %s", name));
  }

  private Optional<TextRepoFile> findFileForDocument(Document doc, short typeId) {
    return documentFiles().findFile(doc.getId(), typeId);
  }

  private Supplier<TextRepoFile> createNewFileForDocument(Document doc, short typeId) {
    return () -> {
      final var file = new TextRepoFile(idGenerator.get(), typeId);
      files().insert(file.getId(),
          file.getTypeId()); // TODO: implement and use FileDao.save(TextrepoFile file)
      documentFiles().insert(doc.getId(), file.getId());
      return file;
    };
  }

  private TypesDao types() {
    return transaction.attach(TypesDao.class);
  }

  private FilesDao files() {
    return transaction.attach(FilesDao.class);
  }

  private DocumentFilesDao documentFiles() {
    return transaction.attach(DocumentFilesDao.class);
  }
}
