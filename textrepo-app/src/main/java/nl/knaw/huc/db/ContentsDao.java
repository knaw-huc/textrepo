package nl.knaw.huc.db;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.Version;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;
import java.util.UUID;

public interface ContentsDao {

  @SqlUpdate("insert into contents (sha224, content) values (:sha224, :content) on conflict do nothing")
  void insert(@BindBean Contents contents);

  @SqlQuery("select sha224, content from contents where sha224 = ?")
  @RegisterConstructorMapper(value = Contents.class)
  Optional<Contents> findBySha224(@Bind String sha224);

  @SqlQuery("select sha224, content from contents left join versions " +
      "on versions.contents_sha = contents.sha224 where versions.id = ?")
  @RegisterConstructorMapper(value = Contents.class)
  Optional<Contents> findByVersionId(UUID versionId);

}
