package nl.knaw.huc.service.store;

import nl.knaw.huc.api.TextRepoContents;
import nl.knaw.huc.db.ContentsDao;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;

public class JdbiContentsStorage implements ContentsStorage {
  private final Logger logger = LoggerFactory.getLogger(JdbiContentsStorage.class);

  private final Jdbi jdbi;

  public JdbiContentsStorage(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public void storeContents(TextRepoContents contents) {
    try {
      getContentsDao().insert(contents);
    } catch (Exception e) {
      logger.warn("Failed to insert contents: {}", e.getMessage());
      throw new WebApplicationException(e);
    }
  }

  @Override
  public TextRepoContents getBySha224(String sha224) {
    return getContentsDao().findBySha224(sha224).orElseThrow(() -> new NotFoundException("Contents not found"));
  }

  private ContentsDao getContentsDao() {
    return jdbi.onDemand(ContentsDao.class);
  }

}
