package nl.knaw.huc.resources.view.xml;

import nu.xom.Document;
import nu.xom.Nodes;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class SimpleXPathResolver extends XmlResolver {
  private final String xpath;

  public SimpleXPathResolver(String xpath) {
    this.xpath = requireNonNull(xpath);
  }

  @Override
  protected Nodes query(@Nonnull Document xmlDoc) {
    return xmlDoc.query(xpath);
  }
}

