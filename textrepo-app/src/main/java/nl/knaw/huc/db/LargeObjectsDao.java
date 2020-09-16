package nl.knaw.huc.db;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.io.InputStream;


public interface LargeObjectsDao {
  @SqlQuery("insert into large_objects (lob) values (:data) returning id")
  long insert(@Bind("data") InputStream data);

  @SqlQuery("select lob from large_objects where id = :id")
  InputStream read(@Bind("id") long id);

  @SqlUpdate("delete from large_objects where id = :id returning lo_unlink(lob)")
  void delete(@Bind("id") long id);
}
