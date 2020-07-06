package nl.knaw.huc.service;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;

public interface DashboardService {
  int countDocuments();

  int countDocumentsWithoutFiles();

  int countDocumentsWithoutMetadata();

  int countOrphans();

  Page<Document> findOrphans(PageParams pageParams);
}
