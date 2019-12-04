package nl.knaw.huc.service;

import nl.knaw.huc.core.SupportedType;
import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.xml.XMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.copy;

public class FieldsService {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public String createFieldsForType(
      @NotNull InputStream inputStream,
      @NotNull SupportedType supportedType
  ) {
    switch (supportedType) {
      case TXT:
        return fromTxt(inputStream);
      case XML:
        return fromXml(inputStream);
      default:
        throw new IllegalStateException();
    }
  }

  private String fromTxt(@NotNull InputStream inputStream) {
    try {
      return IOUtils.toString(inputStream, UTF_8);
    } catch (IOException e) {
      throw new IllegalArgumentException("Input stream could not be converted to string");
    }
  }

  private String fromXml(InputStream inputStream) {
    var handler = new BodyContentHandler();
    var metadata = new Metadata();
    var parseContext = new ParseContext();

    var baos = new ByteArrayOutputStream();
    try {
      copy(inputStream, baos);
    } catch (IOException ex) {
      throw new IllegalStateException("Could not copy inputstream", ex);
    }
    var bytes = baos.toByteArray();

    logger.info("Contents of the document:" + new String(bytes, UTF_8));

    var xmlParser = new XMLParser();
    try {
      xmlParser.parse(new ByteArrayInputStream(bytes), handler, metadata, parseContext);
    } catch (IOException | SAXException | TikaException ex) {
      throw new IllegalArgumentException("Could not parse xml file", ex);
    }

    var result = handler.toString();
    logger.info("Contents of the document:" + result);
    logger.info("Metadata of the document:");
    var metadataNames = metadata.names();
    for (var name : metadataNames) {
      logger.info(name + ": " + metadata.get(name));
    }
    return result;
  }

}
