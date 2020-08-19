package nl.knaw.huc.service.type;

import nl.knaw.huc.core.Type;

import javax.annotation.Nonnull;
import java.util.List;

public interface TypeService {
  List<Type> list();

  Type create(@Nonnull Type type);

  short getId(String name);

  Type getType(Short typeId);

  Type upsert(Type type);

  void delete(Short typeId);
}
