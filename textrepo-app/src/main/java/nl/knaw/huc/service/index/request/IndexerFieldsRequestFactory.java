package nl.knaw.huc.service.index.request;

import static java.lang.String.format;

import javax.ws.rs.client.Client;
import nl.knaw.huc.service.index.FieldsType;

public class IndexerFieldsRequestFactory {
  private final String url;
  private final Client client;

  public IndexerFieldsRequestFactory(String url, Client client) {
    this.url = url;
    this.client = client;
  }

  public IndexerFieldsRequest build(FieldsType type) {
    switch (type) {
      case ORIGINAL:
        return new OriginalIndexerFieldsRequest(url, client);
      case MULTIPART:
        return new MultipartIndexerFieldsRequest(url, client);
      default:
        throw new IllegalStateException(format("Request type [%s] does not exist", type));
    }
  }
}
