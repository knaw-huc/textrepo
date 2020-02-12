package nl.knaw.huc.service.task;

import org.jdbi.v3.core.Handle;

public interface ProvidesInTransaction<T> {
  T executeIn(Handle transaction);
}
