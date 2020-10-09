package nl.knaw.huc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.TypeRef;
import nl.knaw.huc.api.FormVersion;
import nl.knaw.huc.api.ResultContentsLastModified;
import nl.knaw.huc.api.ResultDoc;
import nl.knaw.huc.api.ResultFields;
import nl.knaw.huc.api.ResultFile;
import nl.knaw.huc.api.ResultMetadataEntry;
import nl.knaw.huc.api.ResultType;
import nl.knaw.huc.api.ResultVersion;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static nl.knaw.huc.service.ResourceUtil.getResource;
import static nl.knaw.huc.service.ResourceUtil.read;

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
  private final int pageSize;

  public FieldsService(
      String textrepoHost,
      ParseContext jsonPath,
      int pageSize
  ) {
    this.textrepoHost = textrepoHost;
    this.jsonPath = jsonPath;
    this.pageSize = pageSize;
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
    var hasDocument = fields.getDoc().getId() != null;
    if (hasDocument) {
      addDocResource(doc);
      addDocMetadataResource(doc.getId(), doc);
    }
    addVersionsResource(fileId, fields);

    return fields;
  }

  private void addFileResource(UUID fileId, ResultDoc doc, ResultType type) {
    var fileJson = getRestResource(FILE_ENDPOINT, fileId);
    String docId = read(fileJson, "$.docId");
    if (docId != null) {
      doc.setId(UUID.fromString(docId));
    }
    type.setId(read(fileJson, "$.typeId"));
  }

  private void addFileMetadataResource(UUID fileId, ResultFile file) {
    var fileMetadataJson = getRestResource(FILE_METADATA_ENDPOINT, fileId);
    Map<String, String> form = read(fileMetadataJson, "$");
    var metadata = form
        .entrySet()
        .stream()
        .map((entry) -> new ResultMetadataEntry(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
    file.setMetadata(metadata);
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

  private void addDocMetadataResource(UUID docId, ResultDoc doc) {
    var docMetadataJson = getRestResource(DOC_METADATA_ENDPOINT, docId);
    Map<String, String> form = read(docMetadataJson, "$");
    var metadata = form
        .entrySet()
        .stream()
        .map((entry) -> new ResultMetadataEntry(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
    doc.setMetadata(metadata);
  }

  private void addVersionsResource(UUID fileId, ResultFields fields) {
    var versions = new ArrayList<ResultVersion>();
    fields.setVersions(versions);
    getAllVersions(fileId, versions);
    setContentsModified(versions);
    fields.setContentsLastModified(createContentsLastModified(fields));
  }

  private void getAllVersions(UUID fileId, ArrayList<ResultVersion> versions) {
    var versionsUrl = createUrl(textrepoHost, fileId, VERSIONS_ENDPOINT);
    var ref = new TypeRef<List<FormVersion>>() {};

    var pageTurner = new PageTurner<>(versionsUrl, 0, pageSize, jsonPath, ref);
    pageTurner.turn(formVersions -> {
      formVersions.forEach((form) -> versions.add(createResultVersion(form)));
    });
  }

  private void setContentsModified(ArrayList<ResultVersion> versions) {
    for (var index = 0; index < versions.size(); index++) {
      versions
          .get(index)
          .setContentsModified(contentsModified(versions, index));
    }
  }

  private ResultVersion createResultVersion(FormVersion form) {
    var result = new ResultVersion();
    result.setId(form.getId());
    result.setSha(form.getSha());
    result.setCreatedAt(form.getCreatedAt());
    return result;
  }

  /**
   * Determine if contents sha of current version has changed with regard to previous version
   * Assume all versions are sorted from newest to oldest
   */
  private boolean contentsModified(List<ResultVersion> allVersions, int currentIndex) {
    var isFirstVersion = allVersions.size() == currentIndex + 1;
    if (isFirstVersion) {
      return true;
    }
    var previous = allVersions.get(currentIndex + 1);
    var current = allVersions.get(currentIndex);
    return !current.getSha().equals(previous.getSha());
  }

  private ResultContentsLastModified createContentsLastModified(ResultFields fields) {
    var firstVersionWithLatestContents = fields
        .getVersions()
        .stream()
        .filter(ResultVersion::getContentsModified)
        .findFirst();

    var result = new ResultContentsLastModified();
    if (firstVersionWithLatestContents.isPresent()) {
      var version = firstVersionWithLatestContents.get();
      result.setContentsSha(version.getSha());
      result.setDateTime(version.getCreatedAt());
      result.setVersionId(version.getId());
    }
    return result;
  }

  private DocumentContext getRestResource(String endpoint, Object id) {
    var url = createUrl(textrepoHost, id, endpoint);
    var request = requestClient
        .target(url)
        .request();
    var response = getResource(url, request);
    return jsonPath.parse(response.readEntity(String.class));
  }

  private String createUrl(String host, Object id, String endpoint) {
    return format(endpoint, host, id);
  }

}