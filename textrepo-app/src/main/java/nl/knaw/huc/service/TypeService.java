package nl.knaw.huc.service;

import nl.knaw.huc.core.Type;

import javax.annotation.Nonnull;
import java.util.List;

public interface TypeService {
  List<String> list();

  short create(@Nonnull Type type);

  short get(String name);
}
