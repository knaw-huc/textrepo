package nl.knaw.huc.textrepo.task;

import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;

import static java.util.Map.of;
import static nl.knaw.huc.textrepo.util.TestUtils.asCodeBlock;
import static nl.knaw.huc.textrepo.util.TestUtils.asHeaderLink;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceInUrlAndQueryParams;

public class TestFindFileContentsByExternalId extends AbstractConcordionTest {

  public String createDocument(String externalId) {
    return RestUtils.createDocument(externalId);
  }

  public String createFile(String docId) {
    return RestUtils.createFile(docId, getTextTypeId());
  }

  public String createVersion(String fileId, String contents) {
    return RestUtils.createVersion(fileId, contents);
  }

  public static class RetrieveResult {
    public int status;
    public String value;
    public String versionHistory;
    public String parent;
    public String type;
    public String headers;
    public String body;
  }

  public RetrieveResult retrieve(String endpoint, String externalId, String fileType) {
    final var response = client
        .target(replaceInUrlAndQueryParams(endpoint,
            of("{externalId}", externalId, "{typeName}", fileType)))
        .request()
        .get();

    var result = new RetrieveResult();
    result.status = response.getStatus();
    result.body = response.readEntity(String.class);

    result.headers = "";
    var links = response.getHeaders().get("Link");
    links.forEach((l) -> {
      System.out.println("link:" + l.toString());
      result.headers += asHeaderLink(l.toString());
    });

    result.versionHistory = links
        .stream()
        .filter(l -> l.toString().contains("/versions"))
        .findFirst()
        .map(l -> "version history")
        .orElse("header link missing");

    result.parent = links
        .stream()
        .filter((l) -> l.toString().contains("/files") && !l.toString().contains("/versions"))
        .findFirst()
        .map(l -> "parent resource")
        .orElse("header link missing");

    result.type = links
        .stream()
        .filter((l) -> l.toString().contains("/types/"))
        .findFirst()
        .map(l -> "type resource")
        .orElse("header link missing");

    result.headers = asCodeBlock(result.headers);

    return result;
  }

}
