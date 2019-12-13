package nl.knaw.huc.service.index;

import io.dropwizard.lifecycle.Managed;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.UUID;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.elasticsearch.common.xcontent.XContentType.JSON;


public class ElasticCustomIndexer implements FileIndexer, Managed {

  private final CustomIndexerConfiguration config;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final ElasticFileIndexer indexer;
  private final Client requestClient = JerseyClientBuilder.newClient();
  private final DocumentBuilder documentBuilder;

  public ElasticCustomIndexer(CustomIndexerConfiguration config) throws CustomIndexerException {
    this.config = config;
    indexer = new ElasticFileIndexer(config.elasticsearch);
    createIndex(config);
    try {
      documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException("Could not create documentBuilder");
    }
    if (config.fields.type.equals("multipart")) {
      requestClient.register(MultiPartFeature.class);
    }
  }

  private void createIndex(CustomIndexerConfiguration config) throws CustomIndexerException {
    logger.info("Creating es index [{}]", config.elasticsearch.index);
    var response = getMapping(config);
    var mappingResult = response.readEntity(String.class);

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

  private Response getMapping(CustomIndexerConfiguration config) throws CustomIndexerException {
    Response response;
    try {
      response = requestClient
          .target(config.mapping)
          .request()
          .get();
      return response;
    } catch (ProcessingException ex) {
      throw new CustomIndexerException(format("Could not fetch mapping from %s", config.mapping), ex);
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

    if (!mimetypeSupported(mimetype)) {
      return;
    }

    var response = getFields(latestVersionContent, mimetype);
    var esFacets = response.readEntity(String.class);

    if (!gotFieldsStatusOk(response, esFacets)) {
      return;
    }

    index(fileId, esFacets);
  }

  private Response getFields(@Nonnull String latestVersionContent, String mimetype) {
    switch (config.fields.type) {
      case "urlencoded":
        return getFieldsUrlencoded(latestVersionContent, mimetype);
      case "multipart":
        return getFieldsMultiparted(latestVersionContent, mimetype);
      default:
        throw new IllegalStateException(format(
            "Fields type [%s] of [%s] does not exist",
            config.elasticsearch.index,
            config.fields.type
        ));
    }
  }

  private Response getFieldsUrlencoded(@Nonnull String latestVersionContent, String mimetype) {
    return requestClient
        .target(config.fields.url)
        .queryParam("mimetype", mimetype)
        .request()
        .post(entity(latestVersionContent, MULTIPART_FORM_DATA));
  }

  private Response getFieldsMultiparted(@Nonnull String latestVersionContent, String mimetype) {
    return postMultiparted(latestVersionContent.getBytes(), mimetype);
  }

  private Response postMultiparted(byte[] bytes, String mimetype) {
    var contentDisposition = FormDataContentDisposition
        .name("file")
        .size(bytes.length)
        .build();

    var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(contentDisposition, bytes, MULTIPART_FORM_DATA_TYPE));

    var request = requestClient
        .target(config.fields.url)
        .queryParam("mimetype", URLEncoder.encode(mimetype, UTF_8))
        .request();

    var entity = entity(multiPart, multiPart.getMediaType());

    return request.post(entity);
  }


  private boolean mimetypeSupported(String mimetype) {
    if (!config.mimetypes.contains(mimetype)) {
      logger.info("Not indexing in {} because {} is not in [{}]",
          config.elasticsearch.index, mimetype, join(", ", config.mimetypes)
      );
      return false;
    }
    return true;
  }

  private boolean gotFieldsStatusOk(Response response, String esFacets) {
    if (response.getStatus() != 200) {
      logger.error(
          "Could not get fields for {}, response was: {} - {}",
          config.elasticsearch.index, response.getStatus(), esFacets
      );
      return false;
    }
    return true;
  }

  private void index(@Nonnull UUID fileId, String esFacets) {
    var indexRequest = new IndexRequest(config.elasticsearch.index)
        .id(fileId.toString())
        .source(esFacets, JSON);
    var response = indexer.indexRequest(indexRequest);
    var status = response.status().getStatus();
    if (status == 201) {
      logger.debug(
          "Succesfully added file {} to index {}",
          fileId, config.elasticsearch.index
      );

    } else {
      logger.error(
          "Response of adding file {} to index {} was: {} - {}",
          fileId, config.elasticsearch.index, status, response.toString()
      );
    }
  }

  /**
   * Tries to detect xml, or else content is text
   * TODO: remove when file.type available
   */
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
