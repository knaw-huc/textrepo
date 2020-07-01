package nl.knaw.huc.service;

import nl.knaw.huc.db.DashboardDao;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbiDashboardService implements DashboardService {
  private static final Logger log = LoggerFactory.getLogger(JdbiDashboardService.class);
  private final Jdbi jdbi;

  public JdbiDashboardService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public long countDocuments() {
    log.info("countDocuments");
    return dashboard().countDocuments();
  }

  @Override
  public long countDocumentsWithoutFiles() {
    log.trace("countDocumentsWithoutFiles");
    return dashboard().countDocumentsWithoutFiles();
  }

  @Override
  public long countDocumentsWithoutMetadata() {
    log.debug("countDocumentsWithoutMetadata");
    return dashboard().countDocumentsWithoutMetadata();
  }

  @Override
  public long countOrphans() {
    return dashboard().countOrphans();
  }

  private DashboardDao dashboard() {
    return jdbi.onDemand(DashboardDao.class);
  }
}
