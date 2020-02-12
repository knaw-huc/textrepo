package nl.knaw.huc.service.task;

import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.FilesDao;
import org.jdbi.v3.core.Handle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.NotFoundException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FindFileTest {
  private static final FilesDao FILES_DAO = mock(FilesDao.class);
  private static final Handle TRANSACTION = mock(Handle.class);
  private static final TextrepoFile TEST_FILE = new TextrepoFile(UUID.randomUUID(), (short) 42);

  @Before
  public void setup() {
    when(TRANSACTION.attach(FilesDao.class)).thenReturn(FILES_DAO);
  }

  @After
  public void resetMocks() {
    reset(FILES_DAO); // to reset verify() counters
  }

  @Test(expected = NullPointerException.class)
  public void testFindFile_rejectsNullQuery() {
    new FindFile(null);
  }

  @Test
  public void testFindFile_usesTransactionToAccessFilesDao() {
    when(FILES_DAO.find(TEST_FILE.getId())).thenReturn(Optional.of(TEST_FILE));
    new FindFile(TEST_FILE.getId()).executeIn(TRANSACTION);
    verify(FILES_DAO).find(TEST_FILE.getId());
  }

  @Test
  public void testFindFile_findsExistingFile() {
    when(FILES_DAO.find(TEST_FILE.getId())).thenReturn(Optional.of(TEST_FILE));
    assertThat(new FindFile(TEST_FILE.getId()).executeIn(TRANSACTION)).isEqualTo(TEST_FILE);
  }

  @Test(expected = NotFoundException.class)
  public void testFindFile_throwsNotFound_whenFileNotFound() {
    when(FILES_DAO.find(any())).thenReturn(Optional.empty());
    new FindFile(TEST_FILE.getId()).executeIn(TRANSACTION);
  }
}
