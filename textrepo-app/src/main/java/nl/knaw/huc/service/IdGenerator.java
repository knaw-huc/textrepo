package nl.knaw.huc.service;

public interface IdGenerator<I> {
  I nextUniqueId();
}
