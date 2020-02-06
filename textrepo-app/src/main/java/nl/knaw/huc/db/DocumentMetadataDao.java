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
  @SqlUpdate("insert into documents_metadata (document_id, key, value) values (:id, :key, :value)")
  void insert(@Bind("id") UUID docId, @BindBean MetadataEntry metadataEntry);

  @SqlQuery("select key, value from documents_metadata where document_id = ?")
  @KeyColumn("key")
  @ValueColumn("value")
  Map<String, String> getByDocumentId(@Bind UUID docId);

  @SqlUpdate("insert into documents_metadata (document_id, key, value) values (:id, :key, :value) " +
      "on conflict (document_id, key) do update set value = excluded.value")
  boolean upsert(@Bind("id") UUID docId, @BindBean MetadataEntry metadataEntry);

  @SqlUpdate("delete from documents_metadata where document_id = :id and key = :key")
  void delete(@Bind("id") UUID docId, @Bind("key") String key);
}
