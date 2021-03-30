package nl.knaw.huc.resources;

import nl.knaw.huc.resources.rest.ContentsResource;
import nl.knaw.huc.resources.rest.DocumentMetadataResource;
import nl.knaw.huc.resources.rest.DocumentsResource;
import nl.knaw.huc.resources.rest.FileMetadataResource;
import nl.knaw.huc.resources.rest.FileVersionsResource;
import nl.knaw.huc.resources.rest.FilesResource;
import nl.knaw.huc.resources.rest.TypesResource;
import nl.knaw.huc.resources.rest.VersionsResource;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static javax.ws.rs.core.UriBuilder.fromResource;

public class HeaderLink {
  public static class Rel {
    public static final String ORIGINAL = "original";
    public static final String TYPE = Link.TYPE;
    public static final String UP = "up";
    public static final String VERSION_HISTORY = "version-history";
  }

  public enum Uri {
    CONTENTS(fromResource(ContentsResource.class).path("{sha}")),
    DOCUMENT(fromResource(DocumentsResource.class).path("{id}")),
    DOCUMENT_METADATA(fromResource(DocumentMetadataResource.class)),
    FILE(fromResource(FilesResource.class).path("{id}")),
    FILE_METADATA(fromResource(FileMetadataResource.class)),
    FILE_VERSIONS(fromResource(FileVersionsResource.class)),
    TYPE(fromResource(TypesResource.class).path("{id}")),
    VERSION(fromResource(VersionsResource.class).path("{id}"));

    private final UriBuilder uriBuilder;

    Uri(UriBuilder uriBuilder) {
      this.uriBuilder = uriBuilder;
    }

    public URI build(Object... values) {
      return uriBuilder.build(values);
    }
  }

  /**
   * @return header Link value
   */
  public static Link create(String rel, Uri uri, Object... uriValues) {
    return Link.fromUri(uri.build(uriValues)).rel(rel).build();
  }

}
