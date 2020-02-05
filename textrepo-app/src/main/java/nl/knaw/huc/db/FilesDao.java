package nl.knaw.huc.db;

import nl.knaw.huc.core.TextrepoFile;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;
import java.util.UUID;

public interface FilesDao {
  @SqlUpdate("insert into files (id, type_id) values (:fileId, :typeId)")
  void insert(@Bind("fileId") UUID fileId, @Bind("typeId") short typeId);

  @SqlQuery("select id, type_id from files where id = ?")
  @RegisterConstructorMapper(value = TextrepoFile.class)
  Optional<TextrepoFile> find(UUID id);

  @SqlUpdate("insert into files (id, type_id) values (:id, :typeId) " +
      "on conflict (id) do update set type_id = excluded.type_id")
  void upsert(@BindBean TextrepoFile file);

  @SqlUpdate("delete from files where id = ?")
  void delete(UUID fileId);
}