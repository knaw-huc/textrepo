package nl.knaw.huc.service;

import nl.knaw.huc.api.DocumentCounts;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
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
  public int countDocuments() {
    log.info("countDocuments");
    return dashboard().countDocuments();
  }

  @Override
  public int countDocumentsWithoutFiles() {
    log.trace("countDocumentsWithoutFiles");
    return dashboard().countDocumentsWithoutFiles();
  }

  @Override
  public int countDocumentsWithoutMetadata() {
    log.debug("countDocumentsWithoutMetadata");
    return dashboard().countDocumentsWithoutMetadata();
  }

  @Override
  public int countOrphans() {
    return dashboard().countOrphans();
  }

  @Override
  public DocumentCounts getDocumentCounts() {
    return dashboard().getDocumentCounts();
  }

  @Override
  public Page<Document> findOrphans(PageParams pageParams) {
    return new Page<>(dashboard().findOrphans(pageParams), countOrphans(), pageParams);
  }

  private DashboardDao dashboard() {
    return jdbi.onDemand(DashboardDao.class);
  }
}
