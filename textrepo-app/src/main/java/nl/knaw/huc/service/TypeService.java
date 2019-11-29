package nl.knaw.huc.service;

import java.util.List;

public interface TypeService {
  List<String> list();

  short create(String name);
}
