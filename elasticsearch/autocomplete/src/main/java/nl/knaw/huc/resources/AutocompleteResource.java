package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.service.FieldsService;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.xml.XMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Path("/autocomplete")
public class AutocompleteResource {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final FieldsService fieldsService;

  public AutocompleteResource(FieldsService fieldsService) {
    this.fieldsService = fieldsService;
  }

  @GET
  @Path("/mapping")
  @Timed
  @Produces(APPLICATION_JSON)
  public Response mapping() {
    // TODO: implement
    return null;
  }

  @POST
  @Path("/fields")
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  public Response fields(
      @FormDataParam("contents") InputStream inputStream
  ) {
    var handler = new BodyContentHandler();
    var metadata = new Metadata();
    var parseContext = new ParseContext();

    var xmlparser = new XMLParser();
    try {
      xmlparser.parse(inputStream, handler, metadata, parseContext);
    } catch (IOException | SAXException | TikaException ex) {
      throw new IllegalArgumentException("Could not parse xml file", ex);
    }

    System.out.print("Contents of the document:" + handler.toString());
    System.out.print("Metadata of the document:");
    var metadataNames = metadata.names();
    for (var name : metadataNames) {
      System.out.print(name + ": " + metadata.get(name));
    }

    return null;
  }

}
