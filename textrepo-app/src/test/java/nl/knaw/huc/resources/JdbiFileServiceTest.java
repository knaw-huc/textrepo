package nl.knaw.huc.resources;

import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.db.FileDao;
import nl.knaw.huc.service.FileService;
import nl.knaw.huc.service.JdbiFileService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.reset;

public class JdbiFileServiceTest {
    private static final Jdbi jdbi = mock(Jdbi.class);
    private static final FileDao fileDao = mock(FileDao.class);

    private static final FileService SERVICE_UNDER_TEST = new JdbiFileService(jdbi);

    private static final String sha224 = "55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6";
    private static final String content = "hello test";
    private static final TextRepoFile textRepoFile = new TextRepoFile(
            sha224,
            content.getBytes()
    );

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
    public void testAddFile_insertsFileInDao_underNormalCircumstances() {
        SERVICE_UNDER_TEST.addFile(textRepoFile);
        verify(fileDao).insert(textRepoFile);
    }

    @Test(expected = WebApplicationException.class)
    public void testAddFile_throwsWebApplicationException_whenInternalErrorHappens() {
        doThrow(new RuntimeException("intended behaviour, please ignore")).when(fileDao).insert(any());
        SERVICE_UNDER_TEST.addFile(textRepoFile);
    }

    @Test
    public void testGetBySha224_returnsFile_whenPresent() {
        when(fileDao.findBySha224(sha224)).thenReturn(Optional.of(textRepoFile));
        assertThat(SERVICE_UNDER_TEST.getBySha224(sha224)).isEqualTo(textRepoFile);
    }

    @Test(expected = NotFoundException.class)
    public void testGetBySha224_throwsNotFound_whenAbsent() {
        when(fileDao.findBySha224(any())).thenReturn(Optional.empty());
        SERVICE_UNDER_TEST.getBySha224(sha224);
    }
}
