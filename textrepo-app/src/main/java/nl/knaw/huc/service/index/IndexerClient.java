package nl.knaw.huc.service.index;

import nl.knaw.huc.service.index.config.IndexerConfiguration;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * IndexerClient communicates with an indexer:
 * - request relevant file types
 * - post files to mapping endpoint
 */
public interface IndexerClient {

  /**
   * Convert file contents to ES doc with indexer's fields endpoint
   * @return String ES doc, or empty optional when failed to convert
   */
  Optional<String> fields(@Nonnull UUID file, @Nonnull String mimetype, @Nonnull String contents);

  IndexerConfiguration getConfig();

  /**
   * List of mimetypes supported by indexer
   * When not present, indexer is assumed to support al mimetypes
   */
  Optional<List<String>> getMimetypes();

}
