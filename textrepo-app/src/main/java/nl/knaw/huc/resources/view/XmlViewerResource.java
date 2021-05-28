package nl.knaw.huc.resources.view;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.resources.view.xml.NamespaceAwareXPathResolver;
import nl.knaw.huc.resources.view.xml.SimpleXPathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;

import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/*
 * This class is a Jersey sub-resource. It can be instantiated in other Jersey resources which
 * handle the first part of the URI which is responsible for fetching Contents and selecting
 * which 'view' is requested.
 * <p>
 * This view applies an xpath expression to (XML) Contents and yields the result as a JSON list
 * <p>
 * In XML contents that do not use namespaces, the xpath expression alone suffices. In XML
 * contents that use (a default) namespace, it is necessary to also pass a prefix to be used
 * for that namespace. This is because the underlying xpath technology requires all namespaces
 * to be prefixed and the default namespace used in the contents does not have a prefix.
 * <p>
 * All non-default namespaces can be addressed in the xpath query by the prefix used
 * in the Contents. These namespaces are implicitly added to the query context.
 *
 * @see nl.knaw.huc.resources.view.ViewBuilderFactory
 * @see nl.knaw.huc.resources.view.ViewVersionResource
 */
@Path("") // Without @Path("") this subresource is not resolved during tests
@Produces(APPLICATION_JSON)
public class XmlViewerResource {
  private static final Logger log = LoggerFactory.getLogger(XmlViewerResource.class);

  private final Contents contents;

  public XmlViewerResource(Contents contents) {
    this.contents = requireNonNull(contents);
  }

  @GET
  @Path("xpath/{xpath}")
  public List<String> getXPathWithoutNamespace(
      @PathParam("xpath") @NotBlank @Encoded String encodedXPath
  ) {
    final var decodedXPath = decode(encodedXPath, UTF_8);
    log.debug("Get xpath: [{}]", decodedXPath);

    final var resolver = new SimpleXPathResolver(decodedXPath);

    final var result = resolver.resolve(contents);
    log.debug("Got: {}", result);

    return result;
  }

  @GET
  @Path("xpath/{prefix}/{xpath}")
  public List<String> getXPathWithNamespace(
      @PathParam("xpath") @NotBlank @Encoded String encodedXPath,
      @PathParam("prefix") String defaultNamespacePrefix
  ) {
    final var decodedXPath = decode(encodedXPath, UTF_8);
    log.debug("Get xpath: [{}] with defaultNameSpacePrefix: [{}]", decodedXPath, defaultNamespacePrefix);

    final var resolver = new NamespaceAwareXPathResolver(defaultNamespacePrefix, decodedXPath);

    final var result = resolver.resolve(contents);
    log.debug("Got: {}", result);

    return result;
  }

}
