package nl.knaw.huc.resources.view.xml;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nonnull;
import nu.xom.Document;
import nu.xom.Nodes;

public class SimpleXpathResolver extends XmlResolver {
  private final String xpath;

  public SimpleXpathResolver(String xpath) {
    this.xpath = requireNonNull(xpath);
  }

  @Override
  protected Nodes query(@Nonnull Document xmlDoc) {
    return xmlDoc.query(xpath);
  }
}

