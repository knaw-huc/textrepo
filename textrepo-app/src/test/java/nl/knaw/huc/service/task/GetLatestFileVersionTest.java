package nl.knaw.huc.service.task;

import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.NotFoundException;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetLatestFileVersionTest {
  private static final VersionsDao VERSIONS_DAO = mock(VersionsDao.class);
  private static final Handle TRANSACTION = mock(Handle.class);
  private static final TextrepoFile TEST_FILE = new TextrepoFile(randomUUID(), (short) 42);
  private static final String TEST_SHA = "blablasha";
  private static final Version TEST_VERSION = new Version(randomUUID(), TEST_FILE.getId(), TEST_SHA, now());

  @BeforeEach
  public void setUp() {
    when(TRANSACTION.attach(VersionsDao.class)).thenReturn(VERSIONS_DAO);
  }

  @AfterEach
  public void resetMocks() {
    reset(VERSIONS_DAO); // to reset verify counters
  }

  @Test
  public void testGetLatestFileVersion_rejectsNullQuery() {
    assertThrows(NullPointerException.class, () -> new GetLatestFileVersion(null));
  }

  @Test
  public void testGetLatestFileVersion_usesTransactionToAccessVersionsDao() {
    when(VERSIONS_DAO.findLatestByFileId(any())).thenReturn(Optional.of(mock(Version.class)));
    new GetLatestFileVersion(TEST_FILE).executeIn(TRANSACTION);
    verify(TRANSACTION).attach(VersionsDao.class);
  }

  @Test
  public void testGetLatestFileVersion_findsLatestVersion_forGivenFile() {
    when(VERSIONS_DAO.findLatestByFileId(TEST_FILE.getId())).thenReturn(Optional.of(TEST_VERSION));
    final var suspect = new GetLatestFileVersion(TEST_FILE).executeIn(TRANSACTION);
    assertThat(suspect).isEqualTo(TEST_VERSION);
  }

  @Test
  public void testGetLatestFileVersion_throwsNotFound_whenVersionNotFound() {
    when(VERSIONS_DAO.findLatestByFileId(any())).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> new GetLatestFileVersion(TEST_FILE).executeIn(TRANSACTION));
  }
}
