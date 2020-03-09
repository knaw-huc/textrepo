package nl.knaw.huc.service;

import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.core.TextrepoFile;

import java.util.List;
import java.util.UUID;

public interface DocumentFilesService {
  Page<TextrepoFile> getFilesByDocumentId(UUID docId, PageParams pageParams);
}
