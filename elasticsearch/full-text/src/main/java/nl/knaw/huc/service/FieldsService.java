package nl.knaw.huc.service;

import nl.knaw.huc.FullTextConfiguration;
import nl.knaw.huc.api.Fields;
import nl.knaw.huc.core.SupportedType;
import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.copy;

public class FieldsService {

  private FullTextConfiguration config;
  private AutoDetectParser autoDetectParser = new AutoDetectParser();

  public FieldsService(FullTextConfiguration config) {
    this.config = config;
  }

  public Fields createFields(
      @NotNull InputStream inputStream,
      SupportedType mimetype
  ) {
    switch (mimetype) {
      case TXT:
        return fromTxt(inputStream);
      case XML:
      case ODT:
      case DOCX:
        return parseWithTika(inputStream, autoDetectParser);
      default:
        throw new IllegalStateException(format("Could not create fields for type [%s]", mimetype));
    }
  }

  private Fields fromTxt(@NotNull InputStream inputStream) {
    String text;
    try {
      text = IOUtils.toString(inputStream, UTF_8);
    } catch (IOException e) {
      throw new IllegalArgumentException("Input stream could not be converted to string");
    }
    return new Fields(text);
  }

  private Fields parseWithTika(InputStream inputStream, AbstractParser parser) {
    var bytes = getBytes(inputStream);
    if (bytes.length == 0) {
      return new Fields("");
    }
    var metadata = new Metadata();
    var parseContext = new ParseContext();
    var handler = new BodyContentHandler(-1);
    try {
      parser.parse(new ByteArrayInputStream(bytes), handler, metadata, parseContext);
    } catch (IOException | SAXException | TikaException ex) {
      throw new BadRequestException("Could not parse file using tika", ex);
    }
    return new Fields(handler.toString());
  }

  private byte[] getBytes(InputStream xml) {
    var baos = new ByteArrayOutputStream();
    try {
      copy(xml, baos);
    } catch (IOException ex) {
      throw new BadRequestException("Could not copy inputstream", ex);
    }
    return baos.toByteArray();
  }

}
