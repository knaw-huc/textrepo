package nl.knaw.huc.service;

public interface DashboardService {
  long countDocuments();

  long countDocumentsWithoutFiles();

  long countDocumentsWithoutMetadata();

  long countOrphans();
}
