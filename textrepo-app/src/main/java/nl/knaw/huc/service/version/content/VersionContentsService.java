package nl.knaw.huc.service.version.content;

import java.util.Optional;
import java.util.UUID;
import nl.knaw.huc.core.Contents;

public interface VersionContentsService {

  Contents getByVersionId(UUID versionId);

  Optional<String> getVersionMimetype(UUID versionId);
}
