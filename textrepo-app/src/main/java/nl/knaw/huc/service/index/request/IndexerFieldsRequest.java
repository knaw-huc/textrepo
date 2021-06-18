package nl.knaw.huc.service.index.request;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;
import java.util.UUID;

public interface IndexerFieldsRequest {
  Response requestFields(
      @Nonnull String contents,
      @Nonnull String mimetype,
      @Nonnull UUID fileId
  );
}
