package nl.knaw.huc.service.dashboard;

import java.util.Map;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.DocumentsOverview;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;

public interface DashboardService {
  long countOrphans();

  DocumentsOverview getDocumentsOverview();

  Page<Document> findOrphans(PageParams pageParams);

  Map<String, Integer> countDocumentsByMetadataKey();

  Map<String, Integer> countDocumentsByMetadataValue(String key);
}
