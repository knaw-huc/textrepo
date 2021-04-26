package nl.knaw.huc.resources.view;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.helpers.ContentsHelper;
import nl.knaw.huc.resources.view.xml.NamespaceAwareXPathResolver;
import nl.knaw.huc.resources.view.xml.SimpleXPathResolver;
import nl.knaw.huc.resources.view.xml.XmlResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/*
 * This class is a Jersey sub-resource. It can be instantiated in other Jersey resources which
 * handle the first part of the URI which is responsible for fetching Contents and selecting
 * which 'view' is requested.
 * <p>
 * TODO: description
 *
 * @see nl.knaw.huc.resources.view.ViewBuilderFactory
 * @see nl.knaw.huc.resources.view.ViewVersionResource
 */
@Path("") // Without @Path("") this subresource is not resolved during tests
@Produces(APPLICATION_JSON)
public class XmlViewerResource {
  private static final Logger log = LoggerFactory.getLogger(XmlViewerResource.class);

  private final Contents contents;

  @SuppressWarnings("unused")
  public XmlViewerResource(Contents contents, ContentsHelper contentsHelper) {
    this.contents = requireNonNull(contents);
  }

  @GET
  @Path("xpath/{expr}")
  public Response getXPathWithoutNamespace(
      @HeaderParam(ACCEPT_ENCODING) String acceptEncoding,
      @PathParam("expr") @NotBlank @Encoded String expr
  ) {
    final var xpath = decode(expr, UTF_8);
    final var resolver = new SimpleXPathResolver(xpath);

    return resolve(resolver, contents);
  }

  @GET
  @Path("xpath/{prefix}/{expr}")
  public Response getXPathWithNamespace(
      @HeaderParam(ACCEPT_ENCODING) String acceptEncoding,
      @PathParam("expr") @NotBlank @Encoded String expr,
      @PathParam("prefix") String defaultNamespacePrefix
  ) {
    final var xpath = decode(expr, UTF_8);
    final var resolver = new NamespaceAwareXPathResolver(defaultNamespacePrefix, xpath);

    return resolve(resolver, contents);
  }

  private Response resolve(XmlResolver resolver, Contents contents) {
    final var result = resolver.resolve(contents);
    log.debug("got [{}]", result);
    return Response.ok(result).build();
  }

}
