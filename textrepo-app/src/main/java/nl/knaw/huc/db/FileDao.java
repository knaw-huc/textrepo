package nl.knaw.huc.db;

import nl.knaw.huc.core.TextrepoFile;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.UUID;

public interface FileDao {
  @SqlUpdate("insert into files (id, type_id) values (:fileId, :typeId)")
  void create(@Bind("fileId") UUID fileId, @Bind("typeId") short typeId);

  @SqlQuery("select id, type_id from files where id = ?")
  @RegisterConstructorMapper(value = TextrepoFile.class)
  TextrepoFile find(UUID id);
}
