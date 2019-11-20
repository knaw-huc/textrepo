package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoContents;
import nl.knaw.huc.db.ContentsDao;
import nl.knaw.huc.service.store.ContentsStorage;
import nl.knaw.huc.service.store.JdbiContentsStorage;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JdbiContentsStorageTest {
  private static final Jdbi jdbi = mock(Jdbi.class);
  private static final ContentsDao CONTENTS_DAO = mock(ContentsDao.class);
  private static final ContentsStorage STORE = new JdbiContentsStorage(jdbi);

  private static final String sha224 = "55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6";
  private static final String content = "hello test";
  private static final TextRepoContents TEXT_REPO_CONTENTS = new TextRepoContents(
      sha224,
      content.getBytes()
  );

  @Before
  public void setup() {
    when(jdbi.onDemand(any())).thenReturn(CONTENTS_DAO);
  }

  @After
  public void teardown() {
    reset(jdbi);
    reset(CONTENTS_DAO);
  }

  @Test
  public void testAddFile_insertsFileInDao_underNormalCircumstances() {
    STORE.storeContents(TEXT_REPO_CONTENTS);
    verify(CONTENTS_DAO).insert(TEXT_REPO_CONTENTS);
  }

  @Test(expected = WebApplicationException.class)
  public void testAddFile_throwsWebApplicationException_whenInternalErrorHappens() {
    doThrow(new RuntimeException("intended behaviour, please ignore")).when(CONTENTS_DAO).insert(any());
    STORE.storeContents(TEXT_REPO_CONTENTS);
  }

  @Test
  public void testGetBySha224_returnsFile_whenPresent() {
    when(CONTENTS_DAO.findBySha224(sha224)).thenReturn(Optional.of(TEXT_REPO_CONTENTS));
    assertThat(STORE.getBySha224(sha224)).isEqualTo(TEXT_REPO_CONTENTS);
  }

  @Test(expected = NotFoundException.class)
  public void testGetBySha224_throwsNotFound_whenAbsent() {
    when(CONTENTS_DAO.findBySha224(any())).thenReturn(Optional.empty());
    STORE.getBySha224(sha224);
  }
}
