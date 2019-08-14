package nl.knaw.huc.db;

import nl.knaw.huc.api.Version;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface VersionDAO {

    @SqlUpdate("insert into versions (document_uuid, date, file_sha) values (:documentUuid, :date, :fileSha)")
    void insert(@BindBean Version version);

    @SqlQuery("select document_uuid, date, file_sha from versions where document_uuid = ? and date = ?")
    @RegisterConstructorMapper(value = Version.class)
    Version findByDocumentUuidAndDate(@Bind UUID documentUuid, @Bind LocalDateTime date);

    @SqlQuery("select document_uuid, date, file_sha from versions where document_uuid = ? order by date desc limit 1")
    @RegisterConstructorMapper(value = Version.class)
    Optional<Version> findLatestByDocumentUuid(@Bind UUID documentUuid);

}
