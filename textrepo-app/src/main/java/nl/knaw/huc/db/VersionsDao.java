package nl.knaw.huc.db;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.core.Version;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface VersionsDao {

  @SqlQuery("insert into versions (id, file_id, contents_sha) values (:id, :fileId, :contentsSha) "
      + "returning *")
  @RegisterConstructorMapper(value = Version.class)
  Version insert(@BindBean Version version);

  @SqlQuery("select id, file_id, created_at, contents_sha "
      + "from versions where file_id = ? order by created_at desc limit 1")
  @RegisterConstructorMapper(value = Version.class)
  Optional<Version> findLatestByFileId(@Bind UUID fileId);

  @SqlQuery("select id, file_id, created_at, contents_sha "
      + "from versions where file_id = ? order by created_at desc")
  @RegisterConstructorMapper(value = Version.class)
  List<Version> findByFileId(UUID fileId);

  @SqlQuery("select id, file_id, created_at, contents_sha "
      + "from versions where file_id = :fileId "
      + "and (:createdAfter\\:\\:timestamp is null or created_at >= :createdAfter\\:\\:timestamp) "
      + "order by created_at desc "
      + "limit :limit offset :offset")
  @RegisterConstructorMapper(value = Version.class)
  List<Version> findByFileId(
      @Bind("fileId") UUID fileId,
      @BindBean PageParams pageParams,
      @Bind("createdAfter") LocalDateTime createdAfter
  );

  @SqlQuery("select count(*) from versions "
      + "where file_id = :fileId "
      + "and (:createdAfter\\:\\:timestamp is null or created_at >= :createdAfter\\:\\:timestamp)")
  long countByFileId(
      @Bind("fileId") UUID fileId,
      @Bind("createdAfter") LocalDateTime createdAfter
  );

  @SqlQuery("select id, file_id, created_at, contents_sha "
      + "from versions where id = ? order by created_at asc")
  @RegisterConstructorMapper(value = Version.class)
  Optional<Version> find(UUID id);

  @SqlQuery("select mimetype from types "
      + "left join files on files.type_id = types.id "
      + "left join versions on versions.file_id = files.id "
      + "where versions.id = ?")
  Optional<String> findMimetypeByVersionId(UUID versionId);

  @SqlUpdate("delete from versions where id = ?")
  void delete(UUID id);
}
