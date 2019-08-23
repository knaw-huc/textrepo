package nl.knaw.huc.service;

@FunctionalInterface
public interface CheckedFunction<T, R> {
  R apply(T param) throws ExistsException;
}
