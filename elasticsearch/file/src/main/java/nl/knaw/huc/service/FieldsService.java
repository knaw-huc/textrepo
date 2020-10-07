package nl.knaw.huc.service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.TypeRef;
import nl.knaw.huc.api.FormVersion;
import nl.knaw.huc.api.ResultContentsLastModified;
import nl.knaw.huc.api.ResultDoc;
import nl.knaw.huc.api.ResultFields;
import nl.knaw.huc.api.ResultFile;
import nl.knaw.huc.api.ResultType;
import nl.knaw.huc.api.ResultVersion;
import nl.knaw.huc.exception.TextRepoRequestException;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;

public class FieldsService {

  private static final String FILE_ENDPOINT = "%s/rest/files/%s";
  private static final String FILE_METADATA_ENDPOINT = "%s/rest/files/%s/metadata";
  private static final String DOC_ENDPOINT = "%s/rest/documents/%s";
  private static final String DOC_METADATA_ENDPOINT = "%s/rest/documents/%s/metadata";
  private static final String TYPE_ENDPOINT = "%s/rest/types/%s";

  /**
   * List of versions, newest first:
   */
  private static final String VERSIONS_ENDPOINT = "%s/rest/files/%s/versions";

  private final String textrepoHost;
  private final Client requestClient = JerseyClientBuilder.newClient();

  private final ParseContext jsonPath;

  public FieldsService(
      String textrepoHost,
      ParseContext jsonPath
  ) {
    this.textrepoHost = textrepoHost;
    this.jsonPath = jsonPath;
  }

  public ResultFields createFields(UUID fileId) {
    var fields = new ResultFields();
    var file = new ResultFile();
    fields.setFile(file);
    var doc = new ResultDoc();
    fields.setDoc(doc);
    var type = new ResultType();
    file.setType(type);

    file.setId(fileId);

    addFileResource(fileId, doc, type);
    addFileMetadataResource(fileId, file);
    addTypeResource(type);
    addDocResource(doc);
    addDocMetadataResource(fileId, doc);
    addVersionsResource(fileId, fields);

    return fields;
  }

  private void addFileResource(UUID fileId, ResultDoc doc, ResultType type) {
    var fileJson = getRestResource(FILE_ENDPOINT, fileId);
    doc.setId(UUID.fromString(read(fileJson, "$.docId")));
    type.setId(read(fileJson, "$.typeId"));
  }

  private void addFileMetadataResource(UUID fileId, ResultFile file) {
    var fileMetadataJson = getRestResource(FILE_METADATA_ENDPOINT, fileId);
    file.setMetadata(read(fileMetadataJson, "$"));
  }

  private void addTypeResource(ResultType type) {
    var typeJson = getRestResource(TYPE_ENDPOINT, type.getId());
    type.setName(read(typeJson, "$.name"));
    type.setMimetype(read(typeJson, "$.mimetype"));
  }

  private void addDocResource(ResultDoc doc) {
    var docJson = getRestResource(DOC_ENDPOINT, doc.getId());
    doc.setExternalId(read(docJson, "$.externalId"));
  }

  private void addDocMetadataResource(UUID fileId, ResultDoc doc) {
    var docMetadataJson = getRestResource(DOC_METADATA_ENDPOINT, fileId);
    doc.setMetadata(read(docMetadataJson, "$"));
  }

  private void addVersionsResource(UUID fileId, ResultFields fields) {
    var versionsJson = getRestResource(VERSIONS_ENDPOINT, fileId);
    var form = read(versionsJson, "$.items", new TypeRef<List<FormVersion>>() {});
    fields.setVersions(new ArrayList<>());
    for (var index = 0; index < form.size(); index++) {
      var result = createResultVersion(form, index);
      fields.getVersions().add(result);
    }
    fields.setContentsLastModified(createContentsLastModified(fields));
  }

  private ResultVersion createResultVersion(List<FormVersion> formVersions, int index) {
    var form = formVersions.get(index);
    var result = new ResultVersion();
    result.setId(form.getId());
    result.setSha(form.getSha());
    result.setCreatedAt(form.getCreatedAt());
    result.setContentsChanged(isContentsChanged(formVersions, index, result));
    return result;
  }

  /**
   * Determine if contents of current version has changed with regard to previous version
   * Assume all versions are sorted from newest to oldest
   */
  private boolean isContentsChanged(List<FormVersion> allVersions, int currentIndex, ResultVersion current) {

    var isFirstVersion = allVersions.size() == currentIndex + 1;
    if (isFirstVersion) {
      return true;
    }
    var previous = allVersions.get(currentIndex + 1);
    return !current.getSha().equals(previous.getSha());
  }

  private ResultContentsLastModified createContentsLastModified(ResultFields fields) {
    var firstVersionWithLatestContents = fields
        .getVersions()
        .stream()
        .filter(ResultVersion::getContentsChanged)
        .findFirst()
        .orElseThrow(() -> new NotFoundException("No version with contentsChanged found"));

    var result = new ResultContentsLastModified();
    result.setContentsSha(firstVersionWithLatestContents.getSha());
    result.setDateTime(firstVersionWithLatestContents.getCreatedAt());
    result.setVersionId(firstVersionWithLatestContents.getId());
    return result;
  }

  /**
   * Read jsonpath in parsed doc
   * @throws TextRepoRequestException with msg containing path and json body
   */
  private <T> T read(DocumentContext doc, String path) {
    TypeRef<T> typeRef = new TypeRef<>() {};
    return read(doc, path, typeRef);
  }

  /**
   * Read jsonpath in parsed doc with explicit type
   * @throws TextRepoRequestException with msg containing path and json body
   */
  private <T> T read(DocumentContext doc, String path, TypeRef<T> typeRef) {
    try {
      return doc.read(path, typeRef);
    } catch (Exception ex) {
      throw new TextRepoRequestException(format(
          "Could not get path %s in json %s",
          path, doc.jsonString()
      ), ex);
    }
  }

  private DocumentContext getRestResource(String endpoint, Object id) {
    var url = format(endpoint, textrepoHost, id);
    var response = requestClient
        .target(url)
        .request()
        .get();
    if (response.getStatus() != 200) {
      throw new TextRepoRequestException(format(
          "Unexpected response status of [%s]: got %s instead of 200",
          url, response.getStatus()
      ));
    }
    return jsonPath.parse(response.readEntity(String.class));
  }

}
