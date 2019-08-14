package nl.knaw.huc.resources;

import com.jayway.jsonpath.JsonPath;
import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.db.FileDAO;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


public class FilesResourceTest {

    private static final Jdbi jdbi = mock(Jdbi.class);
    private static final FileDAO fileDao = mock(FileDAO.class);

    private final static String sha224 = "55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6";
    private final static String content = "hello test";
    private final static TextRepoFile textRepoFile = new TextRepoFile(
            sha224,
            content.getBytes()
    );

    @ClassRule
    public static final ResourceTestRule resource = ResourceTestRule
            .builder()
            .addProvider(MultiPartFeature.class)
            .addResource(new FilesResource(jdbi))
            .build();

    @Before
    public void setup() {
        when(jdbi.onDemand(any())).thenReturn(fileDao);
    }

    @After
    public void teardown() {
        reset(jdbi);
        reset(fileDao);
    }

    @Test
    public void testPostFile() {
        var multiPart = new FormDataMultiPart()
                .field("file", content);

        final var request = resource
                .client()
                .register(MultiPartFeature.class)
                .target("/files")
                .request();

        final var entity = Entity.entity(multiPart, multiPart.getMediaType());
        var response = request.post(entity);

        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getHeaderString("Location")).endsWith(sha224);
        String actualSha = responsePart(response, "$.sha224");
        assertThat(actualSha).isEqualTo(sha224);
    }

    @Test
    public void testGetFile() throws IOException {
        when(fileDao.findBySha224(eq(sha224))).thenReturn(Optional.of(textRepoFile));

        var response = resource.client().target("/files/" + sha224).request().get();
        var inputStream = response.readEntity(InputStream.class);
        var actualContent = IOUtils.toString(inputStream, UTF_8);
        assertThat(actualContent).isEqualTo(content);
    }

    @Test
    public void testGetIllegalSha224() {
        var response = resource.client().target("/files/55d4c44f5bc05762d8807f75f3").request().get();
        assertThat(response.getStatus()).isEqualTo(400);
        String actualErrorMessage = responsePart(response, "$.message");
        assertThat(actualErrorMessage).contains("not a sha224");
        assertThat(actualErrorMessage).contains("55d4c44f5bc05762d8807f75f3");
    }

    @Test
    public void testGetNotFound() {
        var response = resource.client().target("/files/" + sha224).request().get();
        assertThat(response.getStatus()).isEqualTo(404);
        String actualErrorMessage = responsePart(response, "$.message");
        assertThat(actualErrorMessage).contains("not found");
    }

    private static String responsePart(Response response, String s) {
        return JsonPath.parse(response.readEntity(String.class)).read(s);
    }

}
