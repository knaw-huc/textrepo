package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;
import org.junit.After;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FileServiceTest {
  private static final FileStoreService STORE = mock(FileStoreService.class);
  private static final FileIndexService INDEX = mock(FileIndexService.class);
  private static final FileService SERVICE_UNDER_TEST = new FileService(STORE, INDEX);

  private static final String SHA224 = "55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6";
  private static final TextRepoFile FILE = mock(TextRepoFile.class);

  @After
  public void resetMocks() {
    reset(STORE, INDEX, FILE);
  }

  @Test
  public void testAddFile_StoresAndIndexes_whenGivenValidFile() {
    SERVICE_UNDER_TEST.addFile(FILE);
    verify(STORE).storeFile(FILE);
    verify(INDEX).indexFile(FILE);
  }

  @Test
  public void testGetBySha224_ReturnsFile_whenFilePresent() {
    when(STORE.getBySha224(SHA224)).thenReturn(FILE);
    assertThat(SERVICE_UNDER_TEST.getBySha224(SHA224)).isEqualTo(FILE);
  }
}