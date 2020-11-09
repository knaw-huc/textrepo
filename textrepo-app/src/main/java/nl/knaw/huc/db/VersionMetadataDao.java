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

public interface VersionMetadataDao {

  @SqlUpdate("insert into versions_metadata (version_id, key, value) values (:id, :key, :value)")
  void insert(@Bind("id") UUID versionId, @BindBean MetadataEntry metadataEntry);

  @SqlUpdate("insert into versions_metadata (version_id, key, value) values (:versionId, :key, :value) " +
      "on conflict (version_id, key) do update set value = excluded.value")
  void upsert(@Bind("versionId") UUID versionId, @BindBean MetadataEntry metadataEntry);

  @SqlUpdate("delete from versions_metadata where version_id = :id and key = :key")
  void delete(@Bind("id") UUID versionId, @Bind("key") String key);

  @SqlQuery("select key, value from versions_metadata where version_id = :id")
  @KeyColumn("key")
  @ValueColumn("value")
  Map<String, String> getMetadataByVersionId(@Bind("id") UUID versionId);
}
