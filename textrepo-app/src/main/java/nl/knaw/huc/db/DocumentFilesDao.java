package nl.knaw.huc.db;

import nl.knaw.huc.core.TextrepoFile;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;
import java.util.UUID;

public interface DocumentFilesDao {
  @SqlUpdate("insert into documents_files (document_id, file_id) values (:docId, :fileId)")
  void insert(@Bind("docId") UUID docId, @Bind("fileId") UUID fileId);

  @SqlQuery("SELECT f.id, f.type_id FROM files f, documents_files df, types t " +
      "WHERE df.document_id = :docId " +
      "AND t.name = :typeName " +
      "AND f.id = df.file_id " +
      "AND t.id = f.type_id")
  @RegisterConstructorMapper(TextrepoFile.class)
  Optional<TextrepoFile> findFile(@Bind("docId") UUID docId, @Bind("typeName") String typeName);

  @SqlQuery("SELECT f.id, f.type_id FROM files f, documents_files df " +
      "WHERE df.document_id = :docId AND f.id = df.file_id AND f.type_id = :typeId")
  @RegisterConstructorMapper(TextrepoFile.class)
  Optional<TextrepoFile> findFile(@Bind("docId") UUID docId, @Bind("typeId") short typeId);

  @SqlQuery("select distinct id from documents where external_id = :externalId")
  Optional<UUID> findDocumentByExternalId(@Bind("externalId") String externalId);

  @SqlQuery("select distinct document_id from documents where file_id = :fileId")
  Optional<UUID> findDocumentId(@Bind("fileId") UUID fileId);

  /**
   * Insert file id and document id
   * When file id already exists, update document id
   */
  @SqlUpdate("insert into documents_files (document_id, file_id) values (:docId, :fileId) " +
      "on conflict (file_id) do update set value = excluded.document_id")
  void upsert(UUID docId, UUID fileId);
}
