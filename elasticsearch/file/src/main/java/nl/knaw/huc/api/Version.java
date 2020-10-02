package nl.knaw.huc.api;

import java.time.LocalDateTime;
import java.util.UUID;

public class Version {
  private UUID id;
  private LocalDateTime createdAt;
  private String sha;
  private Boolean contentsChanged;
}
