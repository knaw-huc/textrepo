package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.TypeDao;
import org.jdbi.v3.core.Handle;

import javax.ws.rs.NotFoundException;
import java.util.function.Function;
import java.util.function.Supplier;

public class FindDocumentFileByType implements Function<Handle, TextrepoFile> {
  private final Document document;
  private final String typeName;

  public FindDocumentFileByType(Document document, String typeName) {
    this.document = document;
    this.typeName = typeName;
  }

  @Override
  public TextrepoFile apply(Handle transaction) {
    final var types = transaction.attach(TypeDao.class);
    final var typeId = types.find(typeName).orElseThrow(illegalType(typeName));

    final var filesForDocument = transaction.attach(DocumentFilesDao.class);
    return filesForDocument.findFile(document.getId(), typeId)
                           .orElseThrow(fileNotFound(document));
  }

  private Supplier<NotFoundException> illegalType(String name) {
    return () -> new NotFoundException(String.format("Illegal type: %s", name));
  }

  private Supplier<NotFoundException> fileNotFound(Document document) {
    return () -> new NotFoundException(
        String.format("No %s file found for document with externalId: %s", typeName, document.getExternalId()));
  }

}
