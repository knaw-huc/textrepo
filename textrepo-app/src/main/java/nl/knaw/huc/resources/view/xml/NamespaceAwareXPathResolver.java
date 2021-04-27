package nl.knaw.huc.resources.view.xml;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class NamespaceAwareXPathResolver extends XmlResolver {
  private static final Logger log = LoggerFactory.getLogger(NamespaceAwareXPathResolver.class);

  private final String defaultNamespacePrefix;
  private final String xpath;

  public NamespaceAwareXPathResolver(String defaultNamespacePrefix, String xpath) {
    this.defaultNamespacePrefix = requireNonNull(defaultNamespacePrefix);
    this.xpath = requireNonNull(xpath);
  }

  @Override
  protected Nodes query(@Nonnull Document xmlDoc) {
    final var root = xmlDoc.getRootElement();
    log.debug("root element: [{}] has namespace prefix: [{}] and uri: [{}]",
        root.getLocalName(),
        root.getNamespacePrefix(),
        root.getNamespaceURI());

    final XPathContext context = createXPathContext(root);

    return xmlDoc.query(xpath, context);
  }

  private XPathContext createXPathContext(@Nonnull Element root) {
    final var declarationCount = root.getNamespaceDeclarationCount();
    log.debug("root element namespace declarationCount: {}", declarationCount);

    final XPathContext context = new XPathContext();
    for (int curIndex = 0; curIndex < declarationCount; curIndex++) {
      final var curPrefix = root.getNamespacePrefix(curIndex);
      final var curUri = root.getNamespaceURI(curPrefix);
      if (log.isTraceEnabled()) {
        log.trace(String.format(" %02d: prefix=%s, uri=%s%n", curIndex, curPrefix, curUri));
      }

      final String prefix;
      if (StringUtils.isBlank(curPrefix)) {
        prefix = defaultNamespacePrefix;
      } else {
        prefix = curPrefix;
      }

      log.trace("adding namespace: [{}] -> [{}]", prefix, curUri);
      context.addNamespace(prefix, curUri);
    }

    return context;
  }
}

