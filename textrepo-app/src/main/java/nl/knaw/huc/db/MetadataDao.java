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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetadataDao {

  @SqlUpdate("insert into metadata (document_uuid, key, value) values (:documentUuid, :key, :value)")
  void insert(@BindBean MetadataEntry metadataEntry);

  @Transaction
  @SqlUpdate("update metadata set value = :value where document_uuid = :documentUuid and key = :key ")
  void update(@BindBean MetadataEntry metadataEntry);

  @Transaction
  @SqlBatch("insert into metadata (document_uuid, key, value) values (:documentUuid, :key, :value)")
  @BatchChunkSize(1000)
  void bulkInsert(@BindBean List<MetadataEntry> entries);

  @SqlQuery("select document_uuid, key, value from metadata where document_uuid = ? and key = ?")
  @RegisterConstructorMapper(MetadataEntry.class)
  Optional<MetadataEntry> findByDocumentUuidAndKey(@Bind UUID documentUuid, @Bind String key);

  @SqlQuery("select document_uuid, key, value from metadata where document_uuid = ?")
  @RegisterConstructorMapper(MetadataEntry.class)
  List<MetadataEntry> findByDocumentUuid(@Bind UUID documentUuid);

}
