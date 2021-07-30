package nl.knaw.huc.db;

import nl.knaw.huc.core.TextRepoFile;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public interface FilesDao {
  @SqlUpdate("insert into files (id, type_id) values (:fileId, :typeId)")
  void insert(@Bind("fileId") UUID fileId, @Bind("typeId") short typeId);

  @SqlQuery("select id, type_id from files where id = ?")
  @RegisterConstructorMapper(value = TextRepoFile.class)
  Optional<TextRepoFile> find(UUID id);

  @SqlQuery("select id, type_id from files where type_id = :typeId")
  @RegisterConstructorMapper(value = TextRepoFile.class)
  void foreachByType(@Bind("typeId") short typeId, Consumer<TextRepoFile> consumer);

  @SqlQuery("select count(id) from files where type_id in (<typeIds>)")
  long countByTypes(@BindList("typeIds") List<Short> typeIds);

  @SqlUpdate("insert into files (id, type_id) values (:id, :typeId) " +
      "on conflict (id) do update set type_id = excluded.type_id")
  void upsert(@BindBean TextRepoFile file);

  @SqlUpdate("delete from files where id = ?")
  void delete(UUID fileId);

  @SqlQuery("select id from files")
  List<UUID> getAll();
}
