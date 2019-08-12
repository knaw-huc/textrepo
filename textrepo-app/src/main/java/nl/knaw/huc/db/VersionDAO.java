package nl.knaw.huc.db;

import nl.knaw.huc.api.Version;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.time.LocalDateTime;
import java.util.UUID;

public interface VersionDAO {

  @SqlUpdate("insert into versions (document_uuid, date, file_sha) values (:document_uuid, :date, :file_sha)")
  void insert(@Bind("document_uuid") UUID documentUuid, @Bind("date") LocalDateTime date, @Bind("file_sha") String file_sha);

  @SqlQuery("select document_uuid, date, file_sha from versions where document_uuid = :document_uuid and date = :date")
  @RegisterConstructorMapper(value = Version.class)
  Version findByDocumentUuidAndDate(@Bind("document_uuid") UUID documentUuid, @Bind("date") LocalDateTime date);

}
