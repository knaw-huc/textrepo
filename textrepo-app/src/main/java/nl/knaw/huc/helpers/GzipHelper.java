package nl.knaw.huc.helpers;

import java.util.zip.GZIPInputStream;

public class GzipHelper {
  // GZIP header magic number, conveniently cast and shifted to bytes 0 and 1 for use in byte[]
  public static final byte GZIP_MAGIC_0 = (byte) GZIPInputStream.GZIP_MAGIC;
  public static final byte GZIP_MAGIC_1 = (byte) (GZIPInputStream.GZIP_MAGIC >> 8);
}
