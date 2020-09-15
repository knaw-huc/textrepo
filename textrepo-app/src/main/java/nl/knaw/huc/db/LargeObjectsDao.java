package nl.knaw.huc.db;

import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.io.InputStream;

public interface LargeObjectsDao {
  @SqlQuery("insert into large_objects (lob) values (:data) returning *")
  long insert(InputStream data);

  @SqlQuery("select lob from large_objects where id = :id")
  InputStream read(long id);

  @SqlUpdate("delete from large_objects where id = :id returning lo_unlink(lob)")
  void delete(long id);
}
