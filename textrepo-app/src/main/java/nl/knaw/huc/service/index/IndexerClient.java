package nl.knaw.huc.service.index;

import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.service.index.config.IndexerConfiguration;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IndexerClient {

  /**
   * Convert file to ES doc with indexer's index endpoint
   * @return String ES doc
   */
  Optional<String> index(@Nonnull UUID file, @Nonnull String mimetype, @Nonnull String contents);

  IndexerConfiguration getConfig();

  /**
   * List of mimetypes supported by indexer
   * When not present, indexer is assumed to support al mimetypes
   */
  Optional<List<String>> getMimetypes();

}
