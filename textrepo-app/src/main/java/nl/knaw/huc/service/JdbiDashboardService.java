package nl.knaw.huc.service;

import nl.knaw.huc.db.DashboardDao;
import org.jdbi.v3.core.Jdbi;

public class JdbiDashboardService implements DashboardService {
  private final Jdbi jdbi;

  public JdbiDashboardService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public long countDocumentsWithoutFiles() {
    return dashboard().countDocumentsWithoutFiles();
  }

  @Override
  public long countDocumentsWithoutMetadata() {
    return dashboard().countDocumentsWithoutMetadata();
  }

  private DashboardDao dashboard() {
    return jdbi.onDemand(DashboardDao.class);
  }
}
