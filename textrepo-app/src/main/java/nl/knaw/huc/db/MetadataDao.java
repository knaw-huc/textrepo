package nl.knaw.huc.db;

import nl.knaw.huc.api.MetadataEntry;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.BatchChunkSize;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface MetadataDao {

  @SqlUpdate("insert into metadata (file_uuid, key, value) values (:id, :key, :value)")
  void insert(@Bind("id") UUID fileId, @BindBean MetadataEntry metadataEntry);

  @Transaction
  @SqlUpdate("update metadata set value = :value where file_uuid = :id and key = :key ")
  void update(@Bind("id") UUID fileId, @BindBean MetadataEntry metadataEntry);

  @Transaction
  @SqlBatch("insert into metadata (file_uuid, key, value) values (:id, :key, :value)")
  @BatchChunkSize(1000)
  void bulkInsert(@Bind("id") UUID fileId, @BindBean Iterator<Map.Entry<String,String>> entries);

  @SqlQuery("select file_uuid, key, value from metadata where file_uuid = ? and key = ?")
  @RegisterConstructorMapper(MetadataEntry.class)
  Optional<MetadataEntry> findByFileUuidAndKey(@Bind UUID fileUuid, @Bind String key);

  @SqlQuery("select file_uuid, key, value from metadata where file_uuid = ?")
  @RegisterConstructorMapper(MetadataEntry.class)
  Iterator<MetadataEntry> findByFileUuid(@Bind UUID fileUuid);

}
