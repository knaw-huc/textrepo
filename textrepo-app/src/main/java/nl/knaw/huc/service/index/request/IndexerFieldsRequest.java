package nl.knaw.huc.service.index.request;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;

public interface IndexerFieldsRequest {
  Response requestFields(
      @Nonnull String contents,
      @Nonnull String mimetype,
      @Nonnull UUID fileId
  );
}
