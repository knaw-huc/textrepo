package nl.knaw.huc.service.index;

import io.dropwizard.lifecycle.Managed;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.UUID;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.elasticsearch.common.xcontent.XContentType.JSON;


public class ElasticCustomFacetIndexer implements FileIndexer, Managed {

  private final CustomFacetIndexerConfiguration config;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final ElasticFileIndexer indexer;
  private final Client jerseyClient = JerseyClientBuilder.newClient();
  private final DocumentBuilder documentBuilder;

  public ElasticCustomFacetIndexer(CustomFacetIndexerConfiguration config) {
    this.config = config;
    indexer = new ElasticFileIndexer(config.elasticsearch);
    createIndex(config);
    try {
      documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException("Could not create documentBuilder");
    }
  }

  private void createIndex(CustomFacetIndexerConfiguration config) {
    var response = jerseyClient
        .target(config.mapping)
        .request()
        .get();

    var mappingResult = response
        .readEntity(String.class);

    if (response.getStatus() != 200) {
      logger.error("Could not get mapping: {} - {}", response.getStatus(), mappingResult);
      return;
    }

    var client = new TextRepoElasticClient(config.elasticsearch);
    var request = new CreateIndexRequest(config.elasticsearch.index)
        .source(mappingResult, JSON);

    try {
      client
          .getClient()
          .indices()
          .create(request, RequestOptions.DEFAULT);
    } catch (ElasticsearchStatusException ex) {
      logger.warn("Could not create index [{}], already exists", config.elasticsearch.index);
    } catch (IOException ex) {
      logger.error("Could not create index [{}]", config.elasticsearch.index, ex);
    }
  }

  /**
   * Index file
   */
  @Override
  public void indexFile(
      @Nonnull UUID fileId,
      @Nonnull String latestVersionContent
  ) {
    // TODO: use file.type
    var mimetype = getMimetype(latestVersionContent);

    if (mimetypeNotSupported(mimetype)) {
      return;
    }

    var response = getFields(latestVersionContent, mimetype);
    var esFacets = response.readEntity(String.class);

    if (statusNotOk(response, esFacets)) {
      return;
    }

    index(fileId, esFacets);
  }

  private Response getFields(@Nonnull String latestVersionContent, String mimetype) {
    return jerseyClient
        .target(config.fields)
        .queryParam("mimetype", mimetype)
        .request()
        .post(entity(latestVersionContent, MULTIPART_FORM_DATA));
  }

  private boolean mimetypeNotSupported(String mimetype) {
    if (!config.mimetypes.contains(mimetype)) {
      logger.info("Not indexing in {} because {} is not in [{}]",
          config.elasticsearch.index, mimetype, join(", ", config.mimetypes)
      );
      return true;
    }
    return false;
  }

  private boolean statusNotOk(Response response, String esFacets) {
    if (response.getStatus() != 200) {
      logger.error(
          "Could not get fields for {}, response was: {} - {}",
          config.elasticsearch.index, response.getStatus(), esFacets
      );
      return true;
    }
    return false;
  }

  private void index(@Nonnull UUID fileId, String esFacets) {
    var indexRequest = new IndexRequest(config.elasticsearch.index)
        .id(fileId.toString())
        .source(esFacets, JSON);
    indexer.indexRequest(indexRequest);
  }

  private String getMimetype(@Nonnull String latestVersionContent) {
    String mimetype;
    try {
      documentBuilder
          .parse(toInputStream(latestVersionContent, UTF_8));
      mimetype = "application/xml";
    } catch (Exception ex) {
      mimetype = "text/plain";
    }
    return mimetype;
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() throws Exception {
    indexer.stop();
  }

}
