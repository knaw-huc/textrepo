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

  @SqlUpdate("insert into document_metadata (document_id, key, value) values (:id, :key, :value)")
  void insert(@Bind("id") UUID fileId, @BindBean MetadataEntry metadataEntry);

  @Transaction
  @SqlUpdate("update document_metadata set value = :value where document_id = :id and key = :key ")
  void update(@Bind("id") UUID fileId, @BindBean MetadataEntry metadataEntry);

  @Transaction
  @SqlBatch("insert into document_metadata (document_id, key, value) values (:id, :key, :value)")
  @BatchChunkSize(1000)
  void bulkInsert(@Bind("id") UUID documentId, @BindBean Iterator<Map.Entry<String, String>> entries);

  @SqlQuery("select document_id, key, value from document_metadata where document_id = ? and key = ?")
  @RegisterConstructorMapper(MetadataEntry.class)
  Optional<MetadataEntry> findByDocumentIdAndKey(@Bind UUID documentId, @Bind String key);

  @SqlQuery("select document_id, key, value from document_metadata where document_id = ?")
  @RegisterConstructorMapper(MetadataEntry.class)
  Iterator<MetadataEntry> findByDocumentId(@Bind UUID documentId);

}
