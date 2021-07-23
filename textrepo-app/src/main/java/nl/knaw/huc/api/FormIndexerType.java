package nl.knaw.huc.api;

import java.util.List;

public record FormIndexerType(String mimetype, List<String> subtypes) {
}
