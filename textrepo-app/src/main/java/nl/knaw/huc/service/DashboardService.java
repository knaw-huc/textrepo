package nl.knaw.huc.service;

public interface DashboardService {
  long countDocumentsWithoutFiles();

  long countDocumentsWithoutMetadata();
}
