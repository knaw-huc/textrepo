package nl.knaw.huc.service.index;

import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.service.index.config.IndexerConfiguration;

import javax.annotation.Nonnull;
import javax.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Indexer {

  /**
   * @return String result message
   */
  Optional<String> index(
      @Nonnull TextRepoFile file,
      @Nonnull String latestVersionContents
  );

  /**
   * @return String result message when http status 200 or 404
   * @throws WebApplicationException when http status is not 200 or 404
   */
  Optional<String> delete(
      @Nonnull UUID fileId
  );

  IndexerConfiguration getConfig();

  /**
   * List of mimetypes supported by indexer
   * When not present, indexer is assumed to support al mimetypes
   */
  Optional<List<String>> getMimetypes();
}
