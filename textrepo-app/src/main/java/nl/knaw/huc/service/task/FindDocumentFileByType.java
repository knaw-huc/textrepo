package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.DocumentFilesDao;
import org.jdbi.v3.core.Handle;

import javax.ws.rs.NotFoundException;
import java.util.function.Function;
import java.util.function.Supplier;

public class FindDocumentFileByType implements Function<Document, TextrepoFile> {
  private final Handle transaction;
  private final String typeName;
  private final short typeId;

  public FindDocumentFileByType(Handle transaction, String typeName, short typeId) {
    this.transaction = transaction;
    this.typeName = typeName;
    this.typeId = typeId;
  }

  @Override
  public TextrepoFile apply(Document document) {
    return docsFiles().findFile(document.getId(), typeId)
                      .orElseThrow(fileNotFound(document));
  }

  private Supplier<NotFoundException> fileNotFound(Document document) {
    return () -> new NotFoundException(
        String.format("No %s file found for document with externalId: %s", typeName, document.getExternalId()));
  }

  private DocumentFilesDao docsFiles() {
    return transaction.attach(DocumentFilesDao.class);
  }
}
