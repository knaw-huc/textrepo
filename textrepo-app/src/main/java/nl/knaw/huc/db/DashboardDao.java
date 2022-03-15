package nl.knaw.huc.db;

import java.util.LinkedHashMap;
import java.util.List;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.DocumentsOverview;
import nl.knaw.huc.core.PageParams;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@UseClasspathSqlLocator
public interface DashboardDao {
  @SqlQuery
  @RegisterConstructorMapper(DocumentsOverview.class)
  DocumentsOverview getDocumentsOverview();

  @SqlQuery
  long countOrphans();

  @SqlQuery
  @RegisterConstructorMapper(Document.class)
  List<Document> findOrphans(@BindBean PageParams pageParams);

  @SqlQuery("documentCountsByMetadataKey") // use LinkedHashMap to preserve SQL order
  @KeyColumn("key")
  @ValueColumn("count")
  LinkedHashMap<String, Integer> countDocumentsByMetadataKey();

  @SqlQuery("documentCountsByMetadataValue") // use LinkedHashMap to preserve SQL order
  @KeyColumn("value")
  @ValueColumn("count")
  LinkedHashMap<String, Integer> countDocumentsByMetadataValue(@Bind("key") String key);

}
