package nl.knaw.huc.service.task.finder;

import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Test;
import org.mockito.invocation.Invocation;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.reset;

public class JdbiFindFileTaskBuilderTest {
  private static final UUID TEST_UUID = UUID.randomUUID();
  private static final Jdbi JDBI = mock(Jdbi.class);

  @After
  public void resetMocks() {
    reset(JDBI);
  }

  @Test
  public void testBuilderYieldsNonNulTask_whenBuilding() {
    JdbiFindFileTaskBuilder sut = new JdbiFindFileTaskBuilder(JDBI);
    assertThat(sut.forFile(TEST_UUID).build()).isNotNull();
  }

  @Test
  public void testBuilderYieldsTask_thatExecutesInSingleTransaction() {
    new JdbiFindFileTaskBuilder(JDBI).forFile(TEST_UUID).build().run();

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