package nl.knaw.huc.service.task;

import org.jdbi.v3.core.Handle;

public interface InTransactionProvider<T> {
  T executeIn(Handle transaction);
}
