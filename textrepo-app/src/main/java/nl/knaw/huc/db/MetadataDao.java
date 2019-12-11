package nl.knaw.huc.db;

import nl.knaw.huc.api.MetadataEntry;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.ValueColumn;
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
  // Document metadata
  @SqlUpdate("insert into documents_metadata (document_id, key, value) values (:id, :key, :value")
  void insertDocumentMetadata(@Bind("id") UUID docId, @BindBean MetadataEntry metadataEntry);

  // File Metadata
  @SqlUpdate("insert into files_metadata (file_id, key, value) values (:id, :key, :value)")
  void insert(@Bind("id") UUID fileId, @BindBean MetadataEntry metadataEntry);

  @Transaction
  @SqlUpdate("update files_metadata set value = :value where file_id = :id and key = :key ")
  void update(@Bind("id") UUID fileId, @BindBean MetadataEntry metadataEntry);

  @Transaction
  @SqlBatch("insert into files_metadata (file_id, key, value) values (:id, :key, :value)")
  @BatchChunkSize(1000)
  void bulkInsert(@Bind("id") UUID fileId, @BindBean Iterator<Map.Entry<String, String>> entries);

  @SqlQuery("select key, value from files_metadata where file_id = ? and key = ?")
  @RegisterConstructorMapper(MetadataEntry.class)
  Optional<MetadataEntry> findByFileIdAndKey(@Bind UUID fileId, @Bind String key);

  @SqlQuery("select key, value from files_metadata where file_id = ?")
  @KeyColumn("key")
  @ValueColumn("value")
  Map<String, String> getAllByFileId(@Bind UUID fileId);
}
