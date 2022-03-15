package nl.knaw.huc.service.task;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;
import nl.knaw.huc.core.Document;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbiRegisterIdentifiersTaskBuilder implements RegisterIdentifiersTaskBuilder {
  private static final Logger log =
      LoggerFactory.getLogger(JdbiRegisterIdentifiersTaskBuilder.class);

  private final Jdbi jdbi;
  private final Supplier<UUID> idGenerator;

  private Stream<String> externalIds;

  public JdbiRegisterIdentifiersTaskBuilder(Jdbi jdbi, Supplier<UUID> idGenerator) {
    this.jdbi = jdbi;
    this.idGenerator = idGenerator;
  }

  @Override
  public RegisterIdentifiersTaskBuilder forExternalIdentifiers(Stream<String> externalIds) {
    this.externalIds = requireNonNull(externalIds);
    return this;
  }

  @Override
  public Task<List<Document>> build() {
    return new JdbiRegisterIdentifiersTask(externalIds);
  }

  private class JdbiRegisterIdentifiersTask implements Task<List<Document>> {
    private final Stream<String> externalIds;

    private JdbiRegisterIdentifiersTask(Stream<String> externalIds) {
      this.externalIds = externalIds;
    }

    @Override
    public List<Document> run() {
      return jdbi.inTransaction(transaction -> {
        final List<Document> internalIds = new ArrayList<>();
        externalIds.forEach(externalId -> {
          final var internalId =
              new RegisterIdentifier(externalId, idGenerator).executeIn(transaction);
          log.debug("Registered internalId={} for externalId={}", internalId, externalId);
          internalIds.add(internalId);
        });
        return internalIds;
      });
    }
  }
}
