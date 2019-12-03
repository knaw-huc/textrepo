package nl.knaw.huc.db;

import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.UUID;

public interface FileDao {
  @SqlUpdate("insert into files (id, type_id) values (:fileId, :typeId)")
  void create(UUID fileId, short typeId);
}
