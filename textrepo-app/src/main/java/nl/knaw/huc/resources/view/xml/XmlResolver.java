package nl.knaw.huc.resources.view.xml;

import static java.lang.String.format;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.ws.rs.BadRequestException;
import nl.knaw.huc.core.Contents;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class XmlResolver {
  private static final Logger log = LoggerFactory.getLogger(XmlResolver.class);

  protected abstract Nodes query(@Nonnull Document xmlDoc);

  public List<String> resolve(@Nonnull Contents contents) {
    final var xmlDoc = parse(contents);

    try {
      return asListOfXmlExcerpts(query(xmlDoc));
    } catch (Exception e) {
      // Generic catch-all for XML failure cases, as there is not much we can do with the specifics.
      log.warn("failed to resolve: {}", e.getMessage());
      throw new BadRequestException(e.getMessage());
    }
  }

  private Document parse(@Nonnull Contents contents) {
    try {
      return new Builder().build(new StringReader(contents.asUtf8String()));
    } catch (ValidityException e) {
      throw new BadRequestException(format("Document is not valid XML: %s", e.getMessage()));
    } catch (ParsingException e) {
      throw new BadRequestException(format("Document is not well-formed: %s", e.getMessage()));
    } catch (IOException e) {
      throw new BadRequestException(format("Failed to fully read document: %s", e.getMessage()));
    }
  }

  private List<String> asListOfXmlExcerpts(@Nonnull Nodes nodes) {
    final var list =
        new ArrayList<String>(); // if there are no nodes, result should be an empty list

    for (var node : nodes) {
      list.add(node.toXML());
    }

    return list;
  }

}
