package nl.knaw.huc.service;

import nl.knaw.huc.core.TextrepoFile;

import java.util.List;
import java.util.UUID;

public interface DocumentFilesService {
  List<TextrepoFile> getFilesByDocumentId(UUID docId);
}
