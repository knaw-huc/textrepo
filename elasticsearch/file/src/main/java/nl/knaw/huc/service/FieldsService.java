package nl.knaw.huc.service;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import nl.knaw.huc.api.ResultDoc;
import nl.knaw.huc.api.ResultFields;
import nl.knaw.huc.api.ResultFile;
import nl.knaw.huc.api.ResultType;
import nl.knaw.huc.exception.TextRepoRequestException;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;
import java.util.UUID;

import static java.lang.String.format;

public class FieldsService {

  private static final String FILE_ENDPOINT = "%s/rest/files/%s";
  private static final String FILE_METADATA_ENDPOINT = "%s/rest/files/%s/metadata";

  private final String textrepoHost;
  private final Client requestClient = JerseyClientBuilder.newClient();

  private final ParseContext jsonPath = JsonPath.using(Configuration
      .builder()
      .mappingProvider(new JacksonMappingProvider())
      .build());

  public FieldsService(String textrepoHost) {
    this.textrepoHost = textrepoHost;
  }

  public ResultFields createFields(
      UUID fileId
  ) {
    var fields = new ResultFields();
    var file = new ResultFile();
    fields.setFile(file);
    var doc = new ResultDoc();
    fields.setDoc(doc);
    var type = new ResultType();
    file.setType(type);

    file.setId(fileId);

    var fileJson = getRestResource(FILE_ENDPOINT, fileId);
    doc.setId(UUID.fromString(read(fileJson, "$.docId")));
    type.setId(read(fileJson, "$.typeId"));

    var fileMetadata = getRestResource(FILE_METADATA_ENDPOINT, fileId);
    file.setMetadata(read(fileMetadata, "$"));

    return fields;
  }

  /**
   * Read jsonpath in doc
   * @throw useful exception on failure
   */
  private <T> T read(DocumentContext doc, String path) {
    try {
      return doc.read(path, new TypeRef<>() {});
    } catch (Exception ex) {
      throw new TextRepoRequestException(format(
          "Could not get path %s in json %s",
          path, doc.jsonString()
      ), ex);
    }
  }

  private DocumentContext getRestResource(String endpoint, UUID uuid) {
    var url = format(endpoint, textrepoHost, uuid);
    var response = requestClient
        .target(url)
        .request()
        .get();
    if (response.getStatus() != 200) {
      throw new TextRepoRequestException(format(
          "Unexpected response status of [%s]: got %s instead of 200",
          url, response.getStatus()
      ));
    }
    return jsonPath.parse(response.readEntity(String.class));
  }

}
