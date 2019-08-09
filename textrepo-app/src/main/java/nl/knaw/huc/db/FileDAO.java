package nl.knaw.huc.db;

import nl.knaw.huc.api.TextRepoFile;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface FileDAO {

  @SqlUpdate("insert into files (sha1, name, content) values (:sha1, :name, :content)")
  void insert(@Bind("sha1") String sha1, @Bind("name") String name, @Bind("content") byte[] content);

  @SqlQuery("select sha1, name, content from files where sha1 = :sha1")
  @RegisterConstructorMapper(value = TextRepoFile.class)
  TextRepoFile findBySha1(@Bind("sha1") String sha1);

}
