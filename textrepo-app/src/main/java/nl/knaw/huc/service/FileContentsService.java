package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.Version;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

import java.util.UUID;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;

public class FileContentsService {

  private final ContentsService contentsService;
  private final VersionService versionService;
  private MetadataService metadataService;

  public FileContentsService(
      ContentsService contentsService,
      VersionService versionService,
      MetadataService metadataService
  ) {
    this.contentsService = contentsService;
    this.versionService = versionService;
    this.metadataService = metadataService;
  }

  public Contents getLatestFile(UUID fileId) {

    var version = versionService
        .findLatestVersion(fileId)
        .orElseThrow(() -> new NotFoundException(format("No such file: %s", fileId)));

    return contentsService.getBySha224(version.getContentsSha());
  }

  public Version replaceFileContents(
      @Nonnull UUID fileId,
      @Nonnull Contents contents,
      String filename
  ) {
    final var currentSha224 = contents.getSha224();

    var version = versionService
        .findLatestVersion(fileId)
        .filter(v -> v.getContentsSha().equals(currentSha224)) // already the current file for this file
        .orElseGet(() -> versionService.insertNewVersion(fileId, contents, filename, now()));

    metadataService.update(fileId, new MetadataEntry("filename", filename));

    return version;
  }

}
