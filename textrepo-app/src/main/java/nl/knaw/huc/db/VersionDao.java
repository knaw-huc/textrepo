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

public interface VersionDao {

  @SqlUpdate("insert into versions (file_uuid, date, contents_sha) values (:fileUuid, :date, :contentsSha)")
  void insert(@BindBean Version version);

  @SqlQuery("select file_uuid, date, contents_sha from versions where file_uuid = ? order by date desc limit 1")
  @RegisterConstructorMapper(value = Version.class)
  Optional<Version> findLatestByFileUuid(@Bind UUID fileUuid);

  @SqlQuery("select file_uuid, date, contents_sha from versions where file_uuid = ? order by date asc")
  @RegisterConstructorMapper(value = Version.class)
  List<Version> findByUuid(UUID uuid);
}
