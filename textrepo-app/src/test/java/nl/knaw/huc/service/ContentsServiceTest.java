package nl.knaw.huc.service;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.service.store.ContentsStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContentsServiceTest {
  private static final ContentsStorage STORE = mock(ContentsStorage.class);
  private static final ContentsService SERVICE_UNDER_TEST = new ContentsService(STORE);

  private static final String SHA224 = "55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6";
  private static final Contents FILE = mock(Contents.class);

  @AfterEach
  public void resetMocks() {
    reset(STORE, FILE);
  }

  @Test
  public void testAddFile_StoresAndIndexes_whenGivenValidFile() {
    SERVICE_UNDER_TEST.addContents(FILE);
    verify(STORE).storeContents(FILE);
  }

  @Test
  public void testGetBySha224_ReturnsFile_whenFilePresent() {
    when(STORE.get(SHA224)).thenReturn(FILE);
    assertThat(SERVICE_UNDER_TEST.getBySha(SHA224)).isEqualTo(FILE);
  }
}
