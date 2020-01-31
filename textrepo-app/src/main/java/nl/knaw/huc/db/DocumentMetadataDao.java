package nl.knaw.huc.db;

import nl.knaw.huc.api.MetadataEntry;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Map;
import java.util.UUID;

public interface DocumentMetadataDao {
  @SqlUpdate("insert into documents_metadata (document_id, key, value) values (:id, :key, :value")
  void insertDocumentMetadata(@Bind("id") UUID docId, @BindBean MetadataEntry metadataEntry);

  @SqlQuery("select key, value from documents_metadata where document_id = ?")
  @KeyColumn("key")
  @ValueColumn("value")
  Map<String, String> getByDocumentId(@Bind UUID docId);

  @SqlUpdate("update documents_metadata set value = :value where document_id = :id and key = :key")
  boolean update(@Bind("id") UUID docId, @BindBean MetadataEntry metadataEntry);
}
