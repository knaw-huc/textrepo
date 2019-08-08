package nl.knaw.huc.db;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface FileDAO {
  @SqlUpdate("insert into files (id, name, content) values (:id, :name, :content)")
  void insert(@Bind("id") int id, @Bind("name") String name, @Bind("content") byte[] content);

  @SqlQuery("select name from files where id = :id")
  String findNameById(@Bind("id") int id);
}
