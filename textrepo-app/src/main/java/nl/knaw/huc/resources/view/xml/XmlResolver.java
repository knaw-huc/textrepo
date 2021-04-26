package nl.knaw.huc.resources.view.xml;

import nl.knaw.huc.core.Contents;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public abstract class XmlResolver {
  private static final Logger log = LoggerFactory.getLogger(XmlResolver.class);

  protected abstract Nodes query(Document xmlDoc);

  public static Document parse(Contents contents) {
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

  public List<String> resolve(Contents contents) {
    var xmlDoc = parse(contents);
    log.debug("parsed: [{}]", xmlDoc);

    final Nodes nodes;
    try {
      nodes = query(xmlDoc);
    } catch (Exception e) {
      log.warn("xpath failed: {}", e.getMessage());
      throw new BadRequestException(e.getMessage());
    }

    log.debug("xpath yielded {} node(s)", nodes.size());
    return asList(nodes);
  }

  private List<String> asList(Nodes nodes) {
    final var list = new ArrayList<String>();

    for (var node : nodes) {
      list.add(node.toXML());
    }

    return list;
  }

}
