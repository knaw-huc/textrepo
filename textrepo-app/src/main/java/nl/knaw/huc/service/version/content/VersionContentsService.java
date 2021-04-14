package nl.knaw.huc.service.version.content;

import nl.knaw.huc.core.Contents;

import java.util.Optional;
import java.util.UUID;

public interface VersionContentsService {

  Contents getByVersionId(UUID versionId);

  Optional<String> getVersionMimetype(UUID versionId);
}
