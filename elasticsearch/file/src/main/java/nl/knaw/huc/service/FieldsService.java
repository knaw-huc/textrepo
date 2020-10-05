package nl.knaw.huc.service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import nl.knaw.huc.api.ResultDoc;
import nl.knaw.huc.api.ResultFields;
import nl.knaw.huc.api.ResultFile;
import nl.knaw.huc.api.ResultType;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;
import java.util.UUID;

import static java.lang.String.format;

public class FieldsService {

  private static final String FILE_ENDPOINT = "%s/rest/files/%s";

  private final String textrepoHost;
  private final Client requestClient = JerseyClientBuilder.newClient();

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
    doc.setId(UUID.fromString(fileJson.read("$.docId")));
    type.setId(fileJson.read("$.typeId", Integer.class));

    return fields;
  }

  private DocumentContext getRestResource(String endpoint, UUID uuid) {
    String url = format(endpoint, textrepoHost, uuid);
    System.out.println("Url to mock:" + url);
    var request = requestClient
        .target(url)
        .request()
        .get();
    return JsonPath.parse(request.readEntity(String.class));
  }

}
