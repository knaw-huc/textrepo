package nl.knaw.huc.db;

import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface DashboardDao {
  @SqlQuery("select count(*) from documents d where not exists " +
      "(select from documents_files df where df.document_id = d.id)")
  long countDocumentsWithoutFiles();

  @SqlQuery("select count(*) from documents d where not exists " +
      "(select from documents_metadata dm where dm.document_id = d.id)")
  long countDocumentsWithoutMetadata();
}
