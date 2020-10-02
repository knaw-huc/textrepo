package nl.knaw.huc.service;

import nl.knaw.huc.api.Fields;

import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.util.UUID;

public class FieldsService {

  public FieldsService() {
  }

  public Fields createFields(
      UUID fileId
  ) {
    return new Fields(fileId);
  }

}
