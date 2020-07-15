package nl.knaw.huc.db;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.DocumentsOverview;
import nl.knaw.huc.core.PageParams;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface DashboardDao {
  @SqlQuery
  @UseClasspathSqlLocator
  @RegisterConstructorMapper(DocumentsOverview.class)
  DocumentsOverview getDocumentsOverview();

  @SqlQuery
  @UseClasspathSqlLocator
  int countOrphans();

  @SqlQuery
  @UseClasspathSqlLocator
  @RegisterConstructorMapper(Document.class)
  List<Document> findOrphans(@BindBean PageParams pageParams);
}
