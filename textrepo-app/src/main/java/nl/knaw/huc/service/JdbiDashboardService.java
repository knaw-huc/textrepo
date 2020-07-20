package nl.knaw.huc.service;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.DocumentsOverview;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.db.DashboardDao;
import nl.knaw.huc.db.DashboardDao.KeyCount;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class JdbiDashboardService implements DashboardService {

  private final Jdbi jdbi;

  public JdbiDashboardService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public int countOrphans() {
    return dashboard().countOrphans();
  }

  @Override
  public DocumentsOverview getDocumentsOverview() {
    return dashboard().getDocumentsOverview();
  }

  @Override
  public Page<Document> findOrphans(PageParams pageParams) {
    return new Page<>(dashboard().findOrphans(pageParams), countOrphans(), pageParams);
  }

  @Override
  public List<KeyCount> countDocumentsByMetadataKey() {
    return dashboard().countDocumentsByMetadataKey();
  }

  @Override
  public List<DashboardDao.ValueCount> countDocumentsByMetadataValue(String key) {
    return dashboard().countDocumentsByMetadataValue(key);
  }

  private DashboardDao dashboard() {
    return jdbi.onDemand(DashboardDao.class);
  }
}
