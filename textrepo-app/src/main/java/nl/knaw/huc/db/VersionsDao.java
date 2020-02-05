package nl.knaw.huc.db;

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

  @SqlUpdate("insert into versions (file_id, created_at, contents_sha) values (:fileId, :createdAt, :contentsSha)")
  void insert(@BindBean Version version);

  @SqlQuery("select file_id, created_at, contents_sha from versions where file_id = ? order by created_at desc limit 1")
  @RegisterConstructorMapper(value = Version.class)
  Optional<Version> findLatestByFileId(@Bind UUID fileId);

  @SqlQuery("select file_id, created_at, contents_sha from versions where file_id = ? order by created_at asc")
  @RegisterConstructorMapper(value = Version.class)
  List<Version> findByFileId(UUID fileId);
}
