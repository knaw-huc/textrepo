package nl.knaw.huc.db;

import nl.knaw.huc.api.Version;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VersionDao {

  @SqlUpdate("insert into versions (document_uuid, date, contents_sha) values (:documentUuid, :date, :contentsSha)")
  void insert(@BindBean Version version);

  @SqlQuery("select document_uuid, date, contents_sha from versions where document_uuid = ? order by date desc limit 1")
  @RegisterConstructorMapper(value = Version.class)
  Optional<Version> findLatestByDocumentUuid(@Bind UUID documentUuid);

  @SqlQuery("select document_uuid, date, contents_sha from versions where document_uuid = ? order by date asc")
  @RegisterConstructorMapper(value = Version.class)
  List<Version> findByUuid(UUID uuid);
}
