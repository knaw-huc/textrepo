package nl.knaw.huc.service;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.DocumentsOverview;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.db.DashboardDao;

import java.util.List;

public interface DashboardService {
  int countOrphans();

  DocumentsOverview getDocumentsOverview();

  Page<Document> findOrphans(PageParams pageParams);

  List<DashboardDao.KeyCount> documentCountsByMetadataKey();
}
