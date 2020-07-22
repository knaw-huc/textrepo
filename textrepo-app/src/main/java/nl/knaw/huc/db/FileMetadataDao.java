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

public interface FileMetadataDao {
  @SqlUpdate("insert into files_metadata (file_id, key, value) values (:id, :key, :value)")
  void insert(@Bind("id") UUID fileId, @BindBean MetadataEntry metadataEntry);

  @SqlUpdate("insert into files_metadata (file_id, key, value) values (:fileId, :e.key, :e.value) " +
      "on conflict (file_id, key) do update set value = excluded.value")
  void upsert(@Bind("fileId") UUID fileId, @BindBean("e") MetadataEntry metadataEntry);

  @SqlUpdate("delete from files_metadata where file_id = :id and key = :key")
  void delete(@Bind("id") UUID fileId, @Bind("key") String key);

  @SqlQuery("select key, value from files_metadata where file_id = ?")
  @KeyColumn("key")
  @ValueColumn("value")
  Map<String, String> getMetadataByFileId(@Bind UUID fileId);
}
