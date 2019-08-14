package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.db.FileDAO;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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
    private final Logger LOGGER = LoggerFactory.getLogger(FilesResource.class);

    private Jdbi jdbi;

    public FilesResource(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @POST
    @Timed
    @Consumes(MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON)
    public Response postFile(@FormDataParam("file") InputStream uploadedInputStream,
                             @FormDataParam("file") FormDataContentDisposition fileDetail) {

        final TextRepoFile file;
        try {
            file = TextRepoFile.fromContent(uploadedInputStream.readAllBytes());
        } catch (IOException e) {
            LOGGER.warn("Could not read posted file, size={}", fileDetail.getSize());
            throw new BadRequestException("Could not read input stream of posted file", e);
        }

        try {
            getFileDAO().insert(file);
        } catch (Exception e) {
            LOGGER.warn("Failed to insert file: {}", e.getMessage());
            throw new WebApplicationException(e);
        }

        var hash = file.getSha224();
        var location = UriBuilder.fromResource(FilesResource.class).path("{sha224}").build(hash);
        return Response
                .created(location)
                .entity(new AddFileResult(hash))
                .build();
    }

    private static class AddFileResult {
        @JsonProperty
        private final String sha224;

        private AddFileResult(String sha224) {
            this.sha224 = sha224;
        }
    }

    @GET
    @Path("/{sha224}")
    @Timed
    @Produces(APPLICATION_OCTET_STREAM)
    public Response getFileBySha224(@PathParam("sha224") String sha224) {
        if (sha224.length() != 56) {
            LOGGER.warn("bad length in sha224 ({}): {}", sha224.length(), sha224);
            throw new BadRequestException("not a sha224: " + sha224);
        }

        var file = getFileDAO().findBySha224(sha224).orElseThrow(() -> new NotFoundException("File not found"));

        return Response
                .ok(file.getContent(), APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment;")
                .build();
    }

    private FileDAO getFileDAO() {
        return jdbi.onDemand(FileDAO.class);
    }

}