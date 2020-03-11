package nl.knaw.huc.db;

import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.core.Version;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VersionsDao {

  @SqlUpdate("insert into versions (id, file_id, created_at, contents_sha) " +
      "values (:id, :fileId, :createdAt, :contentsSha)")
  void insert(@BindBean Version version);

  @SqlQuery("select id, file_id, created_at, contents_sha " +
      "from versions where file_id = ? order by created_at desc limit 1")
  @RegisterConstructorMapper(value = Version.class)
  Optional<Version> findLatestByFileId(@Bind UUID fileId);

  @SqlQuery("select id, file_id, created_at, contents_sha " +
      "from versions where file_id = ? order by created_at asc")
  @RegisterConstructorMapper(value = Version.class)
  List<Version> findByFileId(UUID fileId);

  @SqlQuery("select id, file_id, created_at, contents_sha " +
      "from versions where file_id = :fileId " +
      "order by created_at asc limit :limit offset :offset")
  @RegisterConstructorMapper(value = Version.class)
  List<Version> findByFileId(@Bind("fileId") UUID fileId, @BindBean PageParams pageParams);

  @SqlQuery("select count(*) from versions where file_id = :fileId")
  int countByFileId(@Bind("fileId") UUID fileId);

  @SqlQuery("select id, file_id, created_at, contents_sha " +
      "from versions where id = ? order by created_at asc")
  @RegisterConstructorMapper(value = Version.class)
  Optional<Version> find(UUID id);

  @SqlUpdate("delete from versions where id = ?")
  void delete(UUID id);
}
