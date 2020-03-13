package nl.knaw.huc.service.task.finder;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.Invocation;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.reset;

public class JdbiFindContentsTaskBuilderTest {
  private static final String TEST_EXTERNAL_ID = "some/id";
  private static final String TEST_TYPE_NAME = "some/type";
  private static final Jdbi JDBI = mock(Jdbi.class);

  @AfterEach
  public void resetMocks() {
    reset(JDBI);
  }

  @Test
  public void testBuilderYieldsNonNulTask_whenBuilding() {
    FindContentsTaskBuilder sut = new JdbiFindContentsTaskBuilder(JDBI)
        .forExternalId(TEST_EXTERNAL_ID)
        .withType(TEST_TYPE_NAME);
    assertThat(sut.build()).isNotNull();
  }

  @Test
  public void testBuilderYieldsTask_thatExecutesInSingleTransaction() {
    new JdbiFindContentsTaskBuilder(JDBI)
        .forExternalId(TEST_EXTERNAL_ID)
        .withType(TEST_TYPE_NAME)
        .build()
        .run();

    long numTransactionsStarted = mockingDetails(JDBI)
        .getInvocations()
        .stream()
        .map(Invocation::getMethod)
        .map(Method::getName)
        .filter(name -> name.matches("inTransaction|useTransaction"))
        .count();

    assertThat(numTransactionsStarted).isEqualTo(1);
  }
}
