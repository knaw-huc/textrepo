package nl.knaw.huc.db;

import nl.knaw.huc.api.TextRepoFile;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface FileDao {

  @SqlUpdate("insert into files (sha224, content) values (:sha224, :content) on conflict do nothing")
  void insert(@BindBean TextRepoFile file);

  @SqlQuery("select sha224, content from files where sha224 = ?")
  @RegisterConstructorMapper(value = TextRepoFile.class)
  Optional<TextRepoFile> findBySha224(@Bind String sha224);

}
