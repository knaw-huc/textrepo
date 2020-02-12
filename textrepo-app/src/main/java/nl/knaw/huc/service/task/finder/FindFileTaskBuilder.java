package nl.knaw.huc.service.task.finder;

import nl.knaw.huc.core.Version;
import nl.knaw.huc.service.task.Task;

import java.util.UUID;

public interface FindFileTaskBuilder {
  FindFileTaskBuilder forFile(UUID fileId);

  Task<Version> build();
}
