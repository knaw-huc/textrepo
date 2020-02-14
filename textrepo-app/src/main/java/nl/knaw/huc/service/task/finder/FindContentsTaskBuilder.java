package nl.knaw.huc.service.task.finder;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.service.task.Task;

public interface FindContentsTaskBuilder {
  FindContentsTaskBuilder forExternalId(String externalId);

  FindContentsTaskBuilder withType(String typeName);

  Task<Contents> build();
}
