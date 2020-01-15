package nl.knaw.huc.service;

import nl.knaw.huc.AutocompleteConfiguration;
import nl.knaw.huc.api.Fields;
import nl.knaw.huc.api.Suggestion;
import nl.knaw.huc.core.SupportedType;
import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.xml.XMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.frequency;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.io.IOUtils.copy;

public class FieldsService {

  private final String keywordDelimiters;
  private final int minKeywordLength;

  public FieldsService(AutocompleteConfiguration config) {
    minKeywordLength = config.getMinKeywordLength();
    keywordDelimiters = config.getKeywordDelimiters();
  }

  public Fields createFieldsForType(
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

  private Fields fromTxt(@NotNull InputStream inputStream) {
    String text;
    try {
      text = IOUtils.toString(inputStream, UTF_8);
    } catch (IOException e) {
      throw new IllegalArgumentException("Input stream could not be converted to string");
    }
    return createFields(text);
  }

  private Fields fromXml(InputStream inputStream) {
    var text = extractText(inputStream);
    return createFields(text);
  }

  private Fields createFields(String text) {
    var suggestionSet = createSuggestions(text);
    return new Fields(suggestionSet);
  }

  private LinkedHashSet<Suggestion> createSuggestions(String text) {
    var tokenizer = new StringTokenizer(text, keywordDelimiters);
    var tokens = new ArrayList<String>();
    while (tokenizer.hasMoreTokens()) {
      var next = tokenizer.nextToken();
      if (next.length() >= this.minKeywordLength) {
        tokens.add(next);
      }
    }

    var tokenSet = new HashSet<>(tokens);

    return tokenSet
        .stream()
        .map((token) -> new Suggestion(token, frequency(tokens, token)))
        .sorted()
        // LinkedHashSet to maintain sorted order:
        .collect(toCollection(LinkedHashSet::new));
  }

  private String extractText(InputStream xml) {
    var handler = new BodyContentHandler(-1);
    var metadata = new Metadata();
    var parseContext = new ParseContext();

    var baos = new ByteArrayOutputStream();
    try {
      copy(xml, baos);
    } catch (IOException ex) {
      throw new IllegalStateException("Could not copy inputstream", ex);
    }
    var bytes = baos.toByteArray();

    var xmlParser = new XMLParser();
    try {
      xmlParser.parse(new ByteArrayInputStream(bytes), handler, metadata, parseContext);
    } catch (IOException | SAXException | TikaException ex) {
      throw new IllegalArgumentException("Could not parse xml file", ex);
    }
    return handler.toString();
  }

}
