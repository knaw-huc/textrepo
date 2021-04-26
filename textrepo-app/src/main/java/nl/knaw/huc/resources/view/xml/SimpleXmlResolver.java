package nl.knaw.huc.resources.view.xml;

import nu.xom.Document;
import nu.xom.Nodes;

public class SimpleXmlResolver extends XmlResolver {
  private final String xpath;

  public SimpleXmlResolver(String xpath) {
    this.xpath = xpath;
  }

  @Override
  protected Nodes query(Document xmlDoc) {
    return xmlDoc.query(xpath);
  }
}

