package nl.knaw.huc.service.index.request;

import static javax.ws.rs.client.Entity.entity;
import static nl.knaw.huc.resources.HeaderLink.Rel.ORIGINAL;
import static nl.knaw.huc.resources.HeaderLink.Uri.FILE;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import nl.knaw.huc.resources.HeaderLink;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

/**
 * Post a file to ./fields with a `multipart/form-data` Content-Type header
 * and a multipart body part named "file" which contains:
 * - file contents
 * - Content-Type header with file mimetype
 */
public class MultipartIndexerFieldsRequest implements IndexerFieldsRequest {

  private final String url;
  private final Client client;

  public MultipartIndexerFieldsRequest(String url, Client client) {
    this.url = url;
    this.client = client;
  }

  @Override
  public Response requestFields(
      @Nonnull String contents,
      @Nonnull String mimetype,
      @Nonnull UUID fileId
  ) {
    return postMultipart(contents.getBytes(), mimetype, fileId);
  }

  private Response postMultipart(byte[] bytes, String mimetype, UUID fileId) {
    var contentDisposition = FormDataContentDisposition
        .name("file")
        .size(bytes.length)
        .build();

    var bodyPart = new FormDataBodyPart(contentDisposition, bytes, MediaType.valueOf(mimetype));
    var originLink = HeaderLink.create(ORIGINAL, FILE, fileId).toString();
    bodyPart.getHeaders().add("Link", originLink);

    var multiPart = new FormDataMultiPart().bodyPart(bodyPart);

    var request = client
        .target(url)
        .request();

    var entity = entity(multiPart, multiPart.getMediaType());
    return request.post(entity);
  }


}
