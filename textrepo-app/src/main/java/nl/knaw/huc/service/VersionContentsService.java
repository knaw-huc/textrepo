package nl.knaw.huc.service;

import nl.knaw.huc.core.Contents;

import java.util.UUID;

public interface VersionContentsService {

  Contents getByVersionId(UUID versionId);

}
