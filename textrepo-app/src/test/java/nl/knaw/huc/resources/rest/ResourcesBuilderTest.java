package nl.knaw.huc.resources.rest;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.huc.TextRepoConfiguration;
import nl.knaw.huc.helpers.Paginator;
import nl.knaw.huc.resources.ResourcesBuilder;
import nl.knaw.huc.resources.view.ViewBuilderFactory;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javax.ws.rs.Path;
import java.util.Collections;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(DropwizardExtensionsSupport.class)
public class ResourcesBuilderTest {

  private final String resourcePackage = "nl.knaw.huc.resources";

  private final Reflections reflections = new Reflections(new ConfigurationBuilder()
      .setUrls(ClasspathHelper.forPackage(resourcePackage))
      .filterInputsBy(new FilterBuilder().includePackage(resourcePackage))
      .setScanners(
          new SubTypesScanner(),
          new TypeAnnotationsScanner()
      ));

  @Test
  public void build_shouldCreateAndReturnAllResources() {

    // Build list of resources:
    var buildResources = new ResourcesBuilder(mock(TextRepoConfiguration.class))
        .contentsHelper(mock(ContentsHelper.class))
        .contentsService(mock(ContentsService.class))
        .dashboardService(mock(DashboardService.class))
        .documentFilesService(mock(DocumentFilesService.class))
        .documentMetadataService(mock(DocumentMetadataService.class))
        .documentService(mock(DocumentService.class))
        .fileService(mock(FileService.class))
        .fileMetadataService(mock(FileMetadataService.class))
        .paginator(mock(Paginator.class))
        .taskBuilderFactory(mock(TaskBuilderFactory.class))
        .typeService(mock(TypeService.class))
        .versionContentsService(mock(VersionContentsService.class))
        .versionMetadataService(mock(VersionMetadataService.class))
        .versionService(mock(VersionService.class))
        .viewBuilderFactory(mock(ViewBuilderFactory.class))
        .build()
        .stream()
        .map(br -> br.getClass().getName())
        .collect(toList());

    // Find all resources annotated with Path:
    var foundResources = reflections
        .getTypesAnnotatedWith(Path.class)
        .stream()
        .map(Class::getName)
        .collect(toList());

    // Check all found resources are created by ResourceBuilder:
    Collections.sort(buildResources);
    Collections.sort(foundResources);
    assertThat(buildResources).isEqualTo(foundResources);
  }


}
