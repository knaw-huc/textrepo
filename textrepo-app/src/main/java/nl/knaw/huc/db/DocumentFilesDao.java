package nl.knaw.huc.db;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.UUID;

public interface DocumentFilesDao {
  @SqlUpdate("insert into document_files (document_id, file_id) values (:docId, :fileId)")
  void insert(@Bind("docId") UUID docId, UUID fileId);
}
