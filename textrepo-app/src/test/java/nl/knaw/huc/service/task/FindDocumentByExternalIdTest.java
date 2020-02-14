package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.db.DocumentsDao;
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

public class FindDocumentByExternalIdTest {
  private static final DocumentsDao DOCUMENTS_DAO = mock(DocumentsDao.class);
  private static final Handle TRANSACTION = mock(Handle.class);
  private static final String TEST_EXTERNAL_ID = "/some/external/ID{maybe=0xCAFEBABE}";
  private static final Document TEST_DOCUMENT = new Document(UUID.randomUUID(), TEST_EXTERNAL_ID);

  @Before
  public void setUp() {
    when(TRANSACTION.attach(DocumentsDao.class)).thenReturn(DOCUMENTS_DAO);
  }

  @After
  public void resetMocks() {
    reset(DOCUMENTS_DAO, TRANSACTION);
  }

  @Test
  public void testFindDocumentByExternalId_usesTransactionToAccessFilesDao() {
    when(DOCUMENTS_DAO.getByExternalId(any())).thenReturn(Optional.of(mock(Document.class)));
    new FindDocumentByExternalId(TEST_EXTERNAL_ID).executeIn(TRANSACTION);
    verify(TRANSACTION).attach(any());
  }

  @Test
  public void testFindDocumentByExternalId_findsExistingDocument() {
    when(DOCUMENTS_DAO.getByExternalId(TEST_EXTERNAL_ID)).thenReturn(Optional.of(TEST_DOCUMENT));
    assertThat(new FindDocumentByExternalId(TEST_EXTERNAL_ID).executeIn(TRANSACTION)).isEqualTo(TEST_DOCUMENT);
  }

  @Test(expected = NotFoundException.class)
  public void testFindDocumentByExternalId_throwsNotFound_whenDocumentNotFound() {
    when(DOCUMENTS_DAO.getByExternalId(any())).thenReturn(Optional.empty());
    new FindDocumentByExternalId(TEST_EXTERNAL_ID).executeIn(TRANSACTION);
  }
}