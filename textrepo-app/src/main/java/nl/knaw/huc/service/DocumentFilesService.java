package nl.knaw.huc.service;

import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.core.TextRepoFile;

import java.util.UUID;

public interface DocumentFilesService {
  Page<TextRepoFile> getFilesByDocumentId(UUID docId, PageParams pageParams);
}
