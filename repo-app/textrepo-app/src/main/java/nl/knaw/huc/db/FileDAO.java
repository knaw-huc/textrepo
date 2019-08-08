package nl.knaw.huc.db;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface FileDAO {
  @SqlUpdate("insert into files (sha1, name, content) values (:sha1, :name, :content)")
  void insert(@Bind("sha1") String sha1, @Bind("name") String name, @Bind("content") byte[] content);

  @SqlQuery("select name from files where sha1 = :sha1")
  String findNameBySha1(@Bind("sha1") String sha1);
}
