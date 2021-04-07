package nl.knaw.huc.textrepo.task;

import nl.knaw.huc.textrepo.AbstractConcordionTest;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Map.of;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.textrepo.util.TestUtils.asCodeBlock;
import static nl.knaw.huc.textrepo.util.TestUtils.asHeaderLink;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceInUrlAndQueryParams;

public class TestImportDocument extends AbstractConcordionTest {
  public static class ImportResult {
    public int status;
    public String versionLink;
    public String fileLink;
    public String documentLink;
    public String contentsLink;
    public String headers;
    public String body;
  }

  public ImportResult retrieve(String endpoint,
                               String externalId,
                               String typeName,
                               String content
  ) {
    final var response = importDocument(endpoint, externalId, content.getBytes(), typeName);

    final var result = new ImportResult();
    result.status = response.getStatus();

    final var body = response.readEntity(String.class);
    result.body = asPrettyJson(body.equals("") ? " " : body);

    result.headers = "";
    final var links = Optional.ofNullable(response.getHeaders().get("Link"))
                              .orElse(Collections.emptyList());
    links.forEach(l -> result.headers += asHeaderLink(l.toString()));
    result.headers = asCodeBlock(result.headers);

    result.versionLink = findLink(links, "version");
    result.fileLink = findLink(links, "file");
    result.documentLink = findLink(links, "document");
    result.contentsLink = findLink(links, "contents");

    return result;
  }

  private String findLink(List<Object> links, String type) {
    return links.stream()
                .map(Object::toString)
                .filter(l -> isRelationshipLinkTo(l, type))
                .findFirst()
                .map(l -> "restful relationship link to " + type)
                .orElse("header link to " + type + " missing");
  }

  private boolean isRelationshipLinkTo(String suspect, String type) {
    return suspect.contains("/rest/" + type)
        && suspect.endsWith("rel=\"" + type + "\"");
  }

  private Response importDocument(String endpoint, String externalId, byte[] content, String typeName) {
    var contentDisposition = FormDataContentDisposition
        .name("contents")
        .fileName(externalId + ".txt")
        .size(content.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(
            contentDisposition,
            new String(content, UTF_8),
            APPLICATION_OCTET_STREAM_TYPE)
        );

    var importUri = replaceInUrlAndQueryParams(endpoint, of(
        "{externalId}", externalId,
        "{typeName}", typeName));
    var request = client
        .register(MultiPartFeature.class)
        .target(importUri)
        .request();

    var entity = entity(multiPart, multiPart.getMediaType());
    return request.post(entity);

  }
}
