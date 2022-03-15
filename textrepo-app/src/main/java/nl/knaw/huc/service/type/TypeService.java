package nl.knaw.huc.service.type;

import java.util.List;
import javax.annotation.Nonnull;
import nl.knaw.huc.core.Type;

public interface TypeService {
  List<Type> list();

  Type create(@Nonnull Type type);

  short getId(String name);

  Type getType(Short typeId);

  Type upsert(Type type);

  void delete(Short typeId);
}
