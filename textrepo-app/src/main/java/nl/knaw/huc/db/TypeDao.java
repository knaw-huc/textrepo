package nl.knaw.huc.db;

import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface TypeDao {
  @SqlUpdate("insert into types (name) values (?)")
  @GetGeneratedKeys
  short create(String name);

  @SqlQuery("select id from types where name = ?")
  Optional<Short> find(String name);

  @SqlQuery("select name from types")
  List<String> list();

  default boolean exists(String name) {
    return find(name).isPresent();
  }
}
