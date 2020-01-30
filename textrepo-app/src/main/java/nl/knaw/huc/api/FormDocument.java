package nl.knaw.huc.api;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class FormDocument {

  private final String externalId;

  public FormDocument(String externalId) {
    this.externalId = externalId;
  }

  public String getExternalId() {
    return externalId;
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("externalId", externalId)
        .toString();
  }
}
