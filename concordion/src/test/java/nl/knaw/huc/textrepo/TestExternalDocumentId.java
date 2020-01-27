package nl.knaw.huc.textrepo;

import nl.knaw.huc.textrepo.util.TestUtils;
import org.concordion.api.MultiValueResult;

import java.net.URL;

import static nl.knaw.huc.textrepo.Config.DOCUMENTS_URL;
import static nl.knaw.huc.textrepo.util.TestUtils.getLocation;
import static nl.knaw.huc.textrepo.util.TestUtils.getStatus;
import static nl.knaw.huc.textrepo.util.TestUtils.postDocumentWithExternalIdAndType;
import static nl.knaw.huc.textrepo.util.TestUtils.putFileWithFilename;

public class TestExternalDocumentId extends AbstractConcordionTest {

  public MultiValueResult upload(String externalId, String type, String text) throws Exception {
    var endpoint = new URL(DOCUMENTS_URL);
    var response = postDocumentWithExternalIdAndType(client(), endpoint, externalId, type, text.getBytes());

    var locationHeader = getLocation(response);
    var optionalFileId = locationHeader.map(location1 -> TestUtils.getDocumentId(location1, type));
    var fileId = optionalFileId.orElse("No file id");

    var location = locationHeader.orElse("No location");
    return new MultiValueResult()
        .with("status", getStatus(response))
        .with("hasLocationHeader", locationHeader
            .map(l -> "has a Location header")
            .orElse("Missing Location header")
        )
        .with("location", location)
        .with("contentsLocation", location.replace("/latest", "/contents"))
        .with("esLocation", "/files/_doc/" + fileId)
        .with("fileId", fileId)
        .with("fileIdIsUUID", optionalFileId.map(TestUtils::isValidUuidMsg).orElse("No file id"));
  }

  public MultiValueResult update(String externalId, String type, String text) throws Exception {
    var endpoint = new URL(DOCUMENTS_URL + "/external-id/" + externalId + "/" + type);
    var response = putFileWithFilename(client(), endpoint, externalId, text.getBytes());

    var locationHeader = getLocation(response);
    var optionalFileId = locationHeader.map(location1 -> TestUtils.getDocumentId(location1, type));
    var fileId = optionalFileId.orElse("No file id");

    var location = locationHeader.orElse("No location");
    return new MultiValueResult()
        .with("status", getStatus(response))
        .with("hasLocationHeader", locationHeader
            .map(l -> "has a Location header")
            .orElse("Missing Location header")
        )
        .with("location", location)
        .with("contentsLocation", location.replace("/latest", "/contents"))
        .with("esLocation", "/files/_doc/" + fileId)
        .with("fileId", fileId)
        .with("fileIdIsUUID", optionalFileId.map(TestUtils::isValidUuidMsg).orElse("No file id"));
  }

}
