package nl.knaw.huc.service.index;

import nl.knaw.huc.service.index.config.ElasticsearchConfiguration;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.elasticsearch.client.RestClient.builder;
import static org.elasticsearch.common.xcontent.XContentType.JSON;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

/**
 * Wrapper around ES RestHighLevelClient
 */
public class TextRepoElasticClient {

  private static final Logger log = LoggerFactory.getLogger(IndexerWithMapping.class);
  final RestHighLevelClient client;
  private final ElasticsearchConfiguration config;

  public TextRepoElasticClient(ElasticsearchConfiguration config) {
    this.config = config;
    var restClientBuilder = builder(config.hosts
        .stream()
        .map(HttpHost::create)
        .collect(toList())
        .toArray(new HttpHost[config.hosts.size()]));
    client = new RestHighLevelClient(restClientBuilder);
  }

  public ElasticsearchConfiguration getConfig() {
    return config;
  }

  /**
   * Insert or update ES doc by file ID
   */
  public Optional<String> upsert(@Nonnull UUID fileId, String esDoc) {
    var indexRequest = new IndexRequest(config.index)
        .id(fileId.toString())
        .source(esDoc, JSON);
    var response = indexRequest(indexRequest);
    return checkIndexStatus(response, fileId);
  }

  private IndexResponse indexRequest(IndexRequest indexRequest) {
    try {
      return client.index(indexRequest, DEFAULT);
    } catch (Exception ex) {
      throw new WebApplicationException("Could not index in Elasticsearch", ex);
    }
  }

  /**
   * When not 200 or 201, return error msg
   */
  private Optional<String> checkIndexStatus(IndexResponse response, UUID fileId) {
    var status = response.status().getStatus();
    var index = config.index;

    if (status == 201) {
      log.debug("Successfully added file [{}] to index [{}]", fileId, index);
      return Optional.empty();
    } else if (status == 200) {
      log.debug("Successfully updated file [{}] to index [{}]", fileId, index);
      return Optional.empty();
    } else {
      var msg = format(
          "Response of adding file %s to index %s was: %d - %s",
          fileId, index, status, response
      );
      log.error(msg);
      return Optional.of(msg);
    }
  }

  /**
   * Delete ES doc by file ID
   */
  public void delete(@Nonnull UUID fileId) {
    var index = config.index;
    log.info(format("Deleting file %s from index %s", fileId, index));
    DeleteResponse response;
    var deleteRequest = new DeleteRequest();
    deleteRequest.index(index);
    deleteRequest.id(fileId.toString());
    try {
      response = client.delete(deleteRequest, DEFAULT);
    } catch (Exception ex) {
      throw new WebApplicationException(format("Could not delete file %s in index %s", fileId, index), ex);
    }
    var status = response.status().getStatus();
    final String msg;
    if (status == 200) {
      msg = format("Successfully deleted file %s from index %s", fileId, index);
    } else if (status == 404) {
      msg = format("File %s not found in index %s", fileId, index);
    } else {
      throw new WebApplicationException(format("Could not delete file %s from index %s", fileId, index));
    }
    log.info(msg);
  }

  /**
   * Retrieve all doc IDs from index using ES scroll api
   */
  public List<UUID> getAllIds() {
    var indexName = config.index;
    try {
      final var scroll = new Scroll(TimeValue.timeValueMinutes(1L));
      var searchRequest = new SearchRequest(indexName);
      searchRequest.scroll(scroll);
      var searchSourceBuilder = new SearchSourceBuilder();
      searchSourceBuilder.query(matchAllQuery());
      searchRequest.source(searchSourceBuilder);

      var searchResponse = client.search(searchRequest, DEFAULT);
      var scrollId = searchResponse.getScrollId();
      var result = getIds(searchResponse);
      var hasHits = result.size() > 0;

      while (hasHits) {
        var scrollRequest = new SearchScrollRequest(scrollId);
        scrollRequest.scroll(scroll);
        searchResponse = client.scroll(scrollRequest, DEFAULT);
        scrollId = searchResponse.getScrollId();
        var newHits = getIds(searchResponse);
        hasHits = newHits.size() > 0;
        result.addAll(newHits);
      }

      log.debug("Found {} files in index {}", result.size(), indexName);
      var clearScrollRequest = new ClearScrollRequest();
      clearScrollRequest.addScrollId(scrollId);
      client.clearScroll(clearScrollRequest, DEFAULT);
      return result;
    } catch (IOException ex) {
      throw new WebApplicationException(format("Could not retrieve IDs from index %s", indexName), ex);
    }
  }

  private List<UUID> getIds(SearchResponse searchResponse) {
    return Arrays
        .stream(searchResponse.getHits().getHits())
        .map(hit -> UUID.fromString(hit.getId()))
        .collect(toList());
  }

  public void createIndex(String mapping) {
    var request = new CreateIndexRequest(config.index)
        .source(mapping, JSON);

    try {
      client
          .indices()
          .create(request, DEFAULT);
    } catch (ElasticsearchStatusException ex) {
      log.info("Could not create index [{}], already exists", config.index);
    } catch (IOException ex) {
      log.error("Could not create index [{}]", config.index, ex);
    }
  }

  public ClusterHealthStatus getHealthStatus() throws IOException {
    var request = new ClusterHealthRequest(config.index);
    var response = client
        .cluster()
        .health(request, DEFAULT);
    return response.getStatus();
  }

}
