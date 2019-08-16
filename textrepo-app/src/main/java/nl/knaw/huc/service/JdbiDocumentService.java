package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.db.FileDao;
import nl.knaw.huc.db.VersionDao;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.UUID;

public class JdbiDocumentService implements DocumentService {
  private final Jdbi jdbi;

  public JdbiDocumentService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Version addDocument(byte[] content) {
    var file = TextRepoFile.fromContent(content);
    getFileDao().insert(file);

    var version = new Version(UUID.randomUUID(), LocalDateTime.now(), file.getSha224());
    getVersionDao().insert(version);

    return version;
  }

  @Override
  public Version getLatestVersion(UUID documentId) {
    return getVersionDao()
            .findLatestByDocumentUuid(documentId)
            .orElseThrow(() -> new NotFoundException("No document for uuid: " + documentId));
  }

  private VersionDao getVersionDao() {
    return jdbi.onDemand(VersionDao.class);
  }

  private FileDao getFileDao() {
    return jdbi.onDemand(FileDao.class);
  }
}
