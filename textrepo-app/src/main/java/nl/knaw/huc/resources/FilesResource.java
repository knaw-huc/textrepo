package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.db.FileDAO;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Path("/files")
public class FilesResource {
    private static final DigestUtils SHA_224 = new DigestUtils(MessageDigestAlgorithms.SHA_224);

    private final Logger LOGGER = LoggerFactory.getLogger(FilesResource.class);

    private Jdbi jdbi;
    private FileDAO fileDAO;

    public FilesResource(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @POST
    @Timed
    @Consumes(MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON)
    public Response postFile(@FormDataParam("file") InputStream uploadedInputStream,
                             @FormDataParam("file") FormDataContentDisposition fileDetail) {
        try {
            var content = uploadedInputStream.readAllBytes();
            var key = SHA_224.digestAsHex(content);

            final var exists = getFileDAO().existsSha224(key);
            LOGGER.debug("exists: " + exists);
            if (exists != null) {
                return Response.ok(new AddFileResult(key)).build();
            }

            try {
                getFileDAO().insert(key, content);
            } catch (JdbiException e) {
                throw new WebApplicationException(e);
            } catch (Exception e) {
                LOGGER.warn("andere ellende: " + e.getMessage());
            }

            return Response
                    .created(UriBuilder.fromResource(FilesResource.class).path("{sha224}").build(key))
//                    .entity(new AddFileResult(key))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Could not read input stream of posted file", e);
        }
    }

    private static class AddFileResult {
        @JsonProperty
        private final String sha;

        private AddFileResult(String sha) {
            this.sha = sha;
        }
    }

    @GET
    @Path("/{sha224}")
    @Timed
    @Produces(APPLICATION_OCTET_STREAM)
    public Response getFileBySha224(@PathParam("sha224") String sha224) {
        var textRepoFile = getFileDAO().findBySha224(sha224);
        return Response
                .ok(textRepoFile.getContent(), APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment;")
                .build();
    }

    // TODO: This should not be the responsibility of a Resource -> migrate to Service layer
    private FileDAO getFileDAO() {
        if (fileDAO == null) {
            var found = jdbi.onDemand(FileDAO.class);
            if (found == null) {
                throw new RuntimeException("No FileDAO handle could be opened by jdbi");
            }
            fileDAO = found;
        }
        return fileDAO;
    }

}