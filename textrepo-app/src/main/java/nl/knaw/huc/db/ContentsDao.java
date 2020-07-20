package nl.knaw.huc.db;

import nl.knaw.huc.core.Contents;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;
import java.util.UUID;

public interface ContentsDao {

  @SqlUpdate("insert into contents (sha224, contents) values (:sha224, :contents) on conflict do nothing")
  void insert(@BindBean Contents contents);

  @SqlQuery("select sha224, contents from contents where sha224 = ?")
  @RegisterConstructorMapper(value = Contents.class)
  Optional<Contents> findBySha224(@Bind String sha224);

  @SqlQuery("select sha224, contents from contents left join versions " +
      "on versions.contents_sha = contents.sha224 where versions.id = ?")
  @RegisterConstructorMapper(value = Contents.class)
  Optional<Contents> findByVersionId(UUID versionId);

  @SqlUpdate("delete from contents where sha224 = ?;")
  void delete(String contentsSha);

}
