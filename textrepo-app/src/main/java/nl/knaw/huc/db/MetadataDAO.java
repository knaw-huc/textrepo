package nl.knaw.huc.db;

import nl.knaw.huc.api.MetadataEntry;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.UUID;

public interface MetadataDAO {

  @SqlUpdate("insert into metadata (document_uuid, key, value) values (:document_uuid, :key, :value)")
  void insert(@Bind("document_uuid") UUID documentUuid, @Bind("key") String key, @Bind("value") String value);

  @SqlQuery("select document_uuid, key, value from metadata where document_uuid = :document_uuid and key = :key")
  @RegisterConstructorMapper(value = MetadataEntry.class)
  MetadataEntry findByDocumentUuidAndKey(@Bind("document_uuid") UUID documentUuid, @Bind("key") String key);

}
