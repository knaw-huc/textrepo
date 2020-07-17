package nl.knaw.huc.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.DocumentsOverview;
import nl.knaw.huc.core.PageParams;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.beans.ConstructorProperties;
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

  @SqlQuery
  @UseClasspathSqlLocator
  @RegisterConstructorMapper(KeyCount.class)
  List<KeyCount> documentCountsByMetadataKey();

  @SqlQuery
  @UseClasspathSqlLocator
  @RegisterConstructorMapper(ValueCount.class)
  List<ValueCount> documentCountsByMetadataValue(@Bind("key") String key);

  class KeyCount {
    private final String key;

    private final int count;

    @ConstructorProperties({"key", "count"})
    public KeyCount(String key, int count) {
      this.key = key;
      this.count = count;
    }

    @JsonProperty
    public String getKey() {
      return key;
    }

    @JsonProperty
    public int getCount() {
      return count;
    }

    @Override
    public String toString() {
      return MoreObjects
          .toStringHelper(this)
          .add("key", key)
          .add("count", count)
          .toString();
    }
  }

  class ValueCount {
    private final String value;

    private final int count;

    @ConstructorProperties({"value", "count"})
    public ValueCount(String value, int count) {
      this.value = value;
      this.count = count;
    }

    @JsonProperty
    public String getValue() {
      return value;
    }

    @JsonProperty
    public int getCount() {
      return count;
    }

    @Override
    public String toString() {
      return MoreObjects
          .toStringHelper(this)
          .add("value", value)
          .add("count", count)
          .toString();
    }
  }
}
