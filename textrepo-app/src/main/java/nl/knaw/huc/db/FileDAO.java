package nl.knaw.huc.db;

import nl.knaw.huc.api.TextRepoFile;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface FileDAO {

  @SqlUpdate("insert into files (sha224, name, content) values (:sha224, :name, :content)")
  void insert(@Bind("sha224") String sha224, @Bind("name") String name, @Bind("content") byte[] content);

  @SqlQuery("select sha224, name, content from files where sha224 = :sha224")
  @RegisterConstructorMapper(value = TextRepoFile.class)
  TextRepoFile findBySha224(@Bind("sha224") String sha224);

}
