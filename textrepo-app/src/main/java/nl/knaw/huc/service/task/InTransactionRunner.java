package nl.knaw.huc.service.task;

import org.jdbi.v3.core.Handle;

public interface InTransactionRunner {
  void executeIn(Handle transaction);
}
