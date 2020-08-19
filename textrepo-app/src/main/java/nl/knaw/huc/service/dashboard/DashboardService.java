package nl.knaw.huc.service.dashboard;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.DocumentsOverview;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;

import java.util.Map;

public interface DashboardService {
  int countOrphans();

  DocumentsOverview getDocumentsOverview();

  Page<Document> findOrphans(PageParams pageParams);

  Map<String, Integer> countDocumentsByMetadataKey();

  Map<String, Integer> countDocumentsByMetadataValue(String key);
}
