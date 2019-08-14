package nl.knaw.huc.db;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.TextRepoFile;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.UUID;

public interface MetadataDAO {

  @SqlUpdate("insert into metadata (document_uuid, key, value) values (:documentUuid, :key, :value)")
  void insert(@BindBean MetadataEntry metadataEntry);

  @SqlQuery("select document_uuid, key, value from metadata where document_uuid = ? and key = ?")
  @RegisterConstructorMapper(value = MetadataEntry.class)
  MetadataEntry findByDocumentUuidAndKey(@Bind UUID documentUuid, @Bind String key);

}
