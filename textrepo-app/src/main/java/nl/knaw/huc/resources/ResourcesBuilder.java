package nl.knaw.huc.resources;

import nl.knaw.huc.TextRepoConfiguration;
import nl.knaw.huc.helpers.ContentsHelper;
import nl.knaw.huc.helpers.Paginator;
import nl.knaw.huc.resources.about.AboutResource;
import nl.knaw.huc.resources.dashboard.DashboardResource;
import nl.knaw.huc.resources.rest.ContentsResource;
import nl.knaw.huc.resources.rest.DocumentFilesResource;
import nl.knaw.huc.resources.rest.DocumentMetadataResource;
import nl.knaw.huc.resources.rest.DocumentsResource;
import nl.knaw.huc.resources.rest.FileMetadataResource;
import nl.knaw.huc.resources.rest.FileVersionsResource;
import nl.knaw.huc.resources.rest.FilesResource;
import nl.knaw.huc.resources.rest.MetadataResource;
import nl.knaw.huc.resources.rest.TypesResource;
import nl.knaw.huc.resources.rest.VersionContentsResource;
import nl.knaw.huc.resources.rest.VersionMetadataResource;
import nl.knaw.huc.resources.rest.VersionsResource;
import nl.knaw.huc.resources.task.DeleteDocumentResource;
import nl.knaw.huc.resources.task.FindResource;
import nl.knaw.huc.resources.task.ImportResource;
import nl.knaw.huc.resources.task.IndexResource;
import nl.knaw.huc.resources.task.RegisterIdentifiersResource;
import nl.knaw.huc.service.contents.ContentsService;
import nl.knaw.huc.service.dashboard.DashboardService;
import nl.knaw.huc.service.document.DocumentService;
import nl.knaw.huc.service.document.files.DocumentFilesService;
import nl.knaw.huc.service.document.metadata.DocumentMetadataService;
import nl.knaw.huc.service.file.FileService;
import nl.knaw.huc.service.file.metadata.FileMetadataService;
import nl.knaw.huc.service.task.TaskBuilderFactory;
import nl.knaw.huc.service.type.TypeService;
import nl.knaw.huc.service.version.VersionService;
import nl.knaw.huc.service.version.content.VersionContentsService;
import nl.knaw.huc.service.version.metadata.VersionMetadataService;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class ResourcesBuilder {
  private final TextRepoConfiguration config;
  private ContentsService contentsService;
  private ContentsHelper contentsHelper;
  private DashboardService dashboardService;
  private Paginator paginator;
  private TaskBuilderFactory taskBuilderFactory;
  private DocumentFilesService documentFilesService;
  private DocumentService documentService;
  private DocumentMetadataService documentMetadataService;
  private FileMetadataService fileMetadataService;
  private VersionService versionService;
  private FileService fileService;
  private TypeService typeService;
  private VersionContentsService versionContentsService;
  private VersionMetadataService versionMetadataService;

  public ResourcesBuilder(@Nonnull TextRepoConfiguration config) {
    this.config = config;
  }

  public ResourcesBuilder contentsService(@Nonnull ContentsService contentsService) {
    this.contentsService = contentsService;
    return this;
  }

  public ResourcesBuilder contentsHelper(@Nonnull ContentsHelper contentsHelper) {
    this.contentsHelper = contentsHelper;
    return this;
  }

  public ResourcesBuilder dashboardService(@Nonnull DashboardService dashboardService) {
    this.dashboardService = dashboardService;
    return this;
  }

  public ResourcesBuilder paginator(@Nonnull Paginator paginator) {
    this.paginator = paginator;
    return this;
  }

  public ResourcesBuilder taskBuilderFactory(@Nonnull TaskBuilderFactory taskBuilderFactory) {
    this.taskBuilderFactory = taskBuilderFactory;
    return this;
  }

  public ResourcesBuilder documentFilesService(@Nonnull DocumentFilesService documentFilesService) {
    this.documentFilesService = documentFilesService;
    return this;
  }

  public ResourcesBuilder documentService(@Nonnull DocumentService documentService) {
    this.documentService = documentService;
    return this;
  }

  public ResourcesBuilder documentMetadataService(@Nonnull DocumentMetadataService documentMetadataService) {
    this.documentMetadataService = documentMetadataService;
    return this;
  }

  public ResourcesBuilder fileMetadataService(@Nonnull FileMetadataService fileMetadataService) {
    this.fileMetadataService = fileMetadataService;
    return this;
  }

  public ResourcesBuilder versionService(@Nonnull VersionService versionService) {
    this.versionService = versionService;
    return this;
  }

  public ResourcesBuilder fileService(@Nonnull FileService fileService) {
    this.fileService = fileService;
    return this;
  }

  public ResourcesBuilder typeService(@Nonnull TypeService typeService) {
    this.typeService = typeService;
    return this;
  }

  public ResourcesBuilder versionContentsService(@Nonnull VersionContentsService versionContentsService) {
    this.versionContentsService = versionContentsService;
    return this;
  }

  public ResourcesBuilder versionMetadataService(@Nonnull VersionMetadataService versionMetadataService) {
    this.versionMetadataService = versionMetadataService;
    return this;
  }

  /**
   * Build TextRepo Jersey resources
   *
   * @return List resources
   */
  public List<Object> build() {
    return Arrays.asList(
        new AboutResource(config),
        new ContentsResource(contentsService, contentsHelper),
        new DashboardResource(dashboardService, paginator),
        new DeleteDocumentResource(taskBuilderFactory),
        new DocumentFilesResource(documentFilesService, paginator),
        new DocumentsResource(documentService, paginator),
        new DocumentMetadataResource(documentMetadataService),
        new FileMetadataResource(fileMetadataService),
        new FileVersionsResource(versionService, paginator),
        new FindResource(taskBuilderFactory, contentsHelper),
        new FilesResource(fileService),
        new ImportResource(taskBuilderFactory),
        new IndexResource(taskBuilderFactory),
        new MetadataResource(documentMetadataService),
        new RegisterIdentifiersResource(taskBuilderFactory),
        new TypesResource(typeService),
        new VersionContentsResource(versionContentsService, contentsHelper),
        new VersionMetadataResource(versionMetadataService),
        new VersionsResource(versionService)
    );

  }

}

