package nl.knaw.huc.helpers.gzip;

import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import static nl.knaw.huc.helpers.gzip.GzipHelper.isGzipped;

public class GzipDetectingInputStream extends PushbackInputStream {
  private boolean isGzipCompressed = false;

  public GzipDetectingInputStream(InputStream in) {
    super(in, 2);

    try {
      byte[] magic = new byte[2];
      int nread = read(magic);
      if (nread > 0) {
        unread(magic, 0, nread);
        isGzipCompressed = isGzipped(magic);
      }
    } catch (IOException e) {
      throw new BadRequestException("Could not read input stream of posted file", e);
    }
  }

  public boolean isGzipCompressed() {
    return isGzipCompressed;
  }
}
