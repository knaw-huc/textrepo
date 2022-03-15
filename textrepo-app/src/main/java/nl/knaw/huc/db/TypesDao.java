package nl.knaw.huc.db;

import java.util.List;
import java.util.Optional;
import nl.knaw.huc.core.Type;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface TypesDao {

  @SqlUpdate("insert into types (name, mimetype) values (:name, :mimetype)")
  @GetGeneratedKeys
  short create(@BindBean Type type);

  @SqlQuery("select id from types where name = ?")
  Optional<Short> findByName(String name);

  @SqlQuery("select id from types where mimetype = ?")
  Optional<Short> findByMimetype(String mimetype);

  @SqlQuery("select id, name, mimetype from types where id = ?")
  @RegisterConstructorMapper(value = Type.class)
  Optional<Type> getById(Short id);

  @SqlQuery("select id, name, mimetype from types where name = ?")
  @RegisterConstructorMapper(value = Type.class)
  Optional<Type> getByName(String name);

  @SqlQuery("select id, name, mimetype from types")
  @RegisterConstructorMapper(value = Type.class)
  List<Type> list();

  default boolean exists(String name) {
    return findByName(name).isPresent();
  }

  @SqlUpdate("insert into types (id, name, mimetype) values (:id, :name, :mimetype) "
      + "on conflict (id) do update set name = excluded.name, mimetype = excluded.mimetype")
  void upsert(@BindBean Type type);

  @SqlUpdate("delete from types where id = :typeId")
  void delete(@Bind("typeId") Short typeId);
}
