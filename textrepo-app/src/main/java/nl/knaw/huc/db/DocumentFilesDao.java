package nl.knaw.huc.db;

import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.core.TextRepoFile;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentFilesDao {
  @SqlUpdate("insert into documents_files (document_id, file_id) values (:docId, :fileId)")
  void insert(@Bind("docId") UUID docId, @Bind("fileId") UUID fileId);

  @SqlQuery("SELECT f.id, f.type_id FROM files f, documents_files df " +
      "WHERE df.document_id = :docId AND f.id = df.file_id AND f.type_id = :typeId")
  @RegisterConstructorMapper(TextRepoFile.class)
  Optional<TextRepoFile> findFile(@Bind("docId") UUID docId, @Bind("typeId") short typeId);

  @SqlQuery("select distinct document_id from documents_files where file_id = :fileId")
  Optional<UUID> findDocumentId(@Bind("fileId") UUID fileId);

  /**
   * Insert file id and document id
   * When file id already exists, update document id
   */
  @SqlUpdate("insert into documents_files (document_id, file_id) values (?, ?) " +
      "on conflict (file_id) do update set document_id = excluded.document_id")
  void upsert(UUID docId, UUID fileId);

  @SqlQuery("select id, type_id " +
      "from documents_files as df left join files as f on f.id = df.file_id " +
      "where df.document_id = ?")
  @RegisterConstructorMapper(TextRepoFile.class)
  List<TextRepoFile> findFilesByDocumentId(UUID docId);

  @SqlQuery("select id, type_id " +
      "from documents_files as df left join files as f on f.id = df.file_id " +
      "where df.document_id = :docId and (:typeId is null or f.type_id = :typeId) " +
      "limit :limit offset :offset")
  @RegisterConstructorMapper(TextRepoFile.class)
  List<TextRepoFile> findFilesByDocumentAndTypeId(
      @Bind("docId") UUID docId,
      @Bind("typeId") Short typeId,
      @BindBean PageParams pageParams
  );

  @SqlQuery("select count(*)" +
      "from documents_files as df left join files as f on f.id = df.file_id " +
      "where df.document_id = :docId and (:typeId is null or f.type_id = :typeId)")
  long countByDocumentAndTypeId(
      @Bind("docId") UUID docId,
      @Nullable @Bind("typeId") Short typeId
  );
}
