package nl.knaw.huc.service.store;

import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.db.FileDao;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;

public class JdbiFileStorage implements FileStorage {
  private final Logger logger = LoggerFactory.getLogger(JdbiFileStorage.class);

  private final Jdbi jdbi;

  public JdbiFileStorage(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public void storeFile(TextRepoFile file) {
    try {
      getFileDao().insert(file);
    } catch (Exception e) {
      logger.warn("Failed to insert file: {}", e.getMessage());
      throw new WebApplicationException(e);
    }
  }

  @Override
  public TextRepoFile getBySha224(String sha224) {
    return getFileDao().findBySha224(sha224).orElseThrow(() -> new NotFoundException("File not found"));
  }

  private FileDao getFileDao() {
    return jdbi.onDemand(FileDao.class);
  }

}
