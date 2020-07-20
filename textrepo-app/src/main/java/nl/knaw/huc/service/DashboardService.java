package nl.knaw.huc.service;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.DocumentsOverview;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.db.DashboardDao.ValueCount;

import java.util.List;

import static nl.knaw.huc.db.DashboardDao.KeyCount;

public interface DashboardService {
  int countOrphans();

  DocumentsOverview getDocumentsOverview();

  Page<Document> findOrphans(PageParams pageParams);

  List<KeyCount> countDocumentsByMetadataKey();

  List<ValueCount> countDocumentsByMetadataValue(String key);
}
