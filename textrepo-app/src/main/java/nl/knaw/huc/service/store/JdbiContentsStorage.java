package nl.knaw.huc.service.store;

import nl.knaw.huc.core.Contents;
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
  public void storeContents(Contents contents) {
    try {
      getContentsDao().insert(contents);
    } catch (Exception e) {
      logger.warn("Failed to insert contents: {}", e.getMessage());
      throw new WebApplicationException(e);
    }
  }

  @Override
  public Contents getBySha(String sha) {
    return getContentsDao()
        .findBySha224(sha)
        .orElseThrow(() -> new NotFoundException("Contents not found"));
  }

  private ContentsDao getContentsDao() {
    return jdbi.onDemand(ContentsDao.class);
  }

}
