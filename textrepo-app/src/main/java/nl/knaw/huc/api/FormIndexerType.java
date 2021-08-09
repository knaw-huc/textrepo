package nl.knaw.huc.api;

import java.util.List;

/**
 * Note: when changing json format of indexer types,
 * also change json schema in indexing documentation
 */
public class FormIndexerType {
  public String mimetype;
  public List<String> subtypes;
}
