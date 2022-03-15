package nl.knaw.huc.service.index.request;

import static javax.ws.rs.client.Entity.entity;
import static nl.knaw.huc.resources.HeaderLink.Rel.ORIGINAL;
import static nl.knaw.huc.resources.HeaderLink.Uri.FILE;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import nl.knaw.huc.resources.HeaderLink;

/**
 * Post a file to ./fields with the request body containing the file contents
 * and the Content-Type header containing the original mimetype
 */
public class OriginalIndexerFieldsRequest implements IndexerFieldsRequest {

  private final String url;
  private final Client client;

  public OriginalIndexerFieldsRequest(String url, Client client) {
    this.url = url;
    this.client = client;
  }

  @Override
  public Response requestFields(
      @Nonnull String contents,
      @Nonnull String mimetype,
      @Nonnull UUID fileId
  ) {
    return client
        .target(url)
        .request()
        .header("Link", HeaderLink.create(ORIGINAL, FILE, fileId))
        .post(entity(contents, mimetype));
  }
}
