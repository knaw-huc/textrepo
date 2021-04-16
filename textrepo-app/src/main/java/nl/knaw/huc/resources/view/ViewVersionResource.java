package nl.knaw.huc.resources.view;

import nl.knaw.huc.helpers.ContentsHelper;
import nl.knaw.huc.service.version.content.VersionContentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.UUID;

/*
 * This class parses the first part of a 'view' URI resolving the requested viewname
 * to a Jersey Subresource which can then handle the view specific details.
 * So, ViewVersionResource is responsible for retrieving the Contents for the
 * requested version and then delegates to a (freshly created) subresource view.
 *
 * @see nl.knaw.huc.resources.view.TextViewerResource
 */
@Path("/view/versions/{versionId}")
public class ViewVersionResource {
  private static final Logger log = LoggerFactory.getLogger(ViewVersionResource.class);

  private final VersionContentsService versionContentsService;
  private final ContentsHelper contentsHelper;
  private final ViewBuilderFactory viewBuilderFactory;

  public ViewVersionResource(VersionContentsService versionContentsService,
                             ContentsHelper contentsHelper,
                             ViewBuilderFactory viewBuilderFactory) {

    this.versionContentsService = versionContentsService;
    this.contentsHelper = contentsHelper;
    this.viewBuilderFactory = viewBuilderFactory;
  }

  @Path("{view}")
  public Object getVersionContentsView(
      @PathParam("versionId") @NotNull @Valid UUID versionId,
      @PathParam("view") @NotNull String view
  ) {
    log.debug("Get version contents: versionId={}", versionId);
    final var contents = versionContentsService.getByVersionId(versionId);
    log.debug("Got version contents: {}", contents);

    log.debug("view: [{}]", view);
    return viewBuilderFactory
        .createView(view)
        .apply(contents, contentsHelper);
  }

}
