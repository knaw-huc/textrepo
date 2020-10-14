package nl.knaw.huc.service;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Link;
import java.util.UUID;
import java.util.regex.Pattern;

public class HeaderLinkUtil {

  // Rough approximation of UUID:
  private static final Pattern UUID_PATTERN = Pattern.compile(".*([0-9a-f-]{36}).*");

  /**
   * Extract file UUID from header link
   */
  public static UUID extractUuid(String headerLinkValue) {
    var url = Link.valueOf(headerLinkValue).getUri().toString();
    var matcher = UUID_PATTERN.matcher(url);
    if (!matcher.find()) {
      throw new BadRequestException("Header Link does not contain file UUID: " + headerLinkValue);
    }
    return UUID.fromString(matcher.group(1));
  }

}
