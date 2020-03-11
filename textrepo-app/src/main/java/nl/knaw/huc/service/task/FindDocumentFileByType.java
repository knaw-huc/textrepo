package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.TypesDao;
import org.jdbi.v3.core.Handle;

import javax.ws.rs.NotFoundException;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class FindDocumentFileByType implements InTransactionProvider<TextrepoFile> {
  private final Document document;
  private final String typeName;

  private Handle transaction;

  public FindDocumentFileByType(Document document, String typeName) {
    this.document = requireNonNull(document);
    this.typeName = requireNonNull(typeName);
  }

  @Override
  public TextrepoFile executeIn(Handle transaction) {
    this.transaction = requireNonNull(transaction);

    final var typeId = types().find(typeName)
                              .orElseThrow(illegalType(typeName));

    return documentFiles().findFile(document.getId(), typeId)
                          .orElseThrow(fileNotFound(document));
  }

  private Supplier<NotFoundException> illegalType(String name) {
    return () -> new NotFoundException(String.format("Illegal type: %s", name));
  }

  private Supplier<NotFoundException> fileNotFound(Document document) {
    return () -> new NotFoundException(
        String.format("No %s file found for document with externalId: %s", typeName, document.getExternalId()));
  }

  private TypesDao types() {
    return transaction.attach(TypesDao.class);
  }

  private DocumentFilesDao documentFiles() {
    return transaction.attach(DocumentFilesDao.class);
  }
}
