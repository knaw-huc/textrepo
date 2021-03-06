package nl.knaw.huc.helpers.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class GzipHelper {
  // GZIP header magic number, conveniently cast and shifted to bytes 0 and 1 for use in byte[]
  public static final byte GZIP_MAGIC_0 = (byte) GZIPInputStream.GZIP_MAGIC;
  public static final byte GZIP_MAGIC_1 = (byte) (GZIPInputStream.GZIP_MAGIC >> 8);

  public static boolean isGzipped(byte[] bytes) {
    return bytes.length > 1 && bytes[0] == GZIP_MAGIC_0 && bytes[1] == GZIP_MAGIC_1;
  }

  public static InputStream decompressIfNeeded(InputStream inputStream) throws IOException {
    GzipDetectingInputStream is = new GzipDetectingInputStream(inputStream);
    if (is.isGzipCompressed()) {
      return new GZIPInputStream(is);
    }
    return is;
  }
}
