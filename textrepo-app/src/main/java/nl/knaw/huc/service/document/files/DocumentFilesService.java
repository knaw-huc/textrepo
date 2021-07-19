package nl.knaw.huc.service.document.files;

import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.core.TextRepoFile;

import java.util.UUID;

public interface DocumentFilesService {
  Page<TextRepoFile> getFilesByDocumentAndTypeId(UUID docId, Short typeId, PageParams pageParams);
}
