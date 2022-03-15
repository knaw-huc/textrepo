package nl.knaw.huc.service.document.files;

import java.util.UUID;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.core.TextRepoFile;

public interface DocumentFilesService {
  Page<TextRepoFile> getFilesByDocumentAndTypeId(UUID docId, Short typeId, PageParams pageParams);
}
