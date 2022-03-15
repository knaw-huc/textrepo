package nl.knaw.huc.service.task;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;
import javax.ws.rs.NotFoundException;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Type;
import nl.knaw.huc.db.DocumentFilesDao;
import org.jdbi.v3.core.Handle;

public class FindDocumentFileByType implements InTransactionProvider<TextRepoFile> {
  private final Document document;
  private final Type type;

  private Handle transaction;

  public FindDocumentFileByType(Document document, Type type) {
    this.document = requireNonNull(document);
    this.type = requireNonNull(type);
  }

  @Override
  public TextRepoFile executeIn(Handle transaction) {
    this.transaction = requireNonNull(transaction);

    return documentFiles().findFile(document.getId(), type.getId())
                          .orElseThrow(fileNotFound(document));
  }

  private Supplier<NotFoundException> fileNotFound(Document document) {
    return () -> new NotFoundException(
        String.format("No %s file found for document with externalId: %s", type.getName(),
            document.getExternalId()));
  }

  private DocumentFilesDao documentFiles() {
    return transaction.attach(DocumentFilesDao.class);
  }
}
