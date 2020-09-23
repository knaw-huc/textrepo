package nl.knaw.huc.core;

import com.google.common.base.MoreObjects;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import javax.annotation.Nonnull;
import javax.ws.rs.BadRequestException;
import java.beans.ConstructorProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import static nl.knaw.huc.helpers.gzip.GzipHelper.isGzipped;
import static nl.knaw.huc.service.contents.ContentsService.abbreviateMiddle;

/**
 * Contents of a file, identified by its sha224-hash
 */
public class Contents {
  private static final DigestUtils SHA_224 = new DigestUtils(MessageDigestAlgorithms.SHA_224);

  private final String sha224;
  private final byte[] contents;

  public static Contents fromBytes(@Nonnull byte[] contents) {
    return new Contents(SHA_224.digestAsHex(contents), contents);
  }

  @ConstructorProperties({"sha224", "contents"})
  public Contents(String sha224, byte[] contents) {
    this.sha224 = sha224;
    this.contents = contents;
  }

  public String getSha224() {
    return sha224;
  }

  public byte[] getContents() {
    return contents;
  }

  public byte[] decompressIfCompressedSizeLessThan(int limit) {
    if (isGzipped(contents) && contents.length < limit) {
      return decompress();
    }
    return contents;
  }

  public String asUtf8String() {
    if (isGzipped(contents)) {
      return new String(decompress(), StandardCharsets.UTF_8);
    }
    return new String(contents, StandardCharsets.UTF_8);
  }

  private byte[] decompress() {
    try {
      return new GZIPInputStream(new ByteArrayInputStream(contents)).readAllBytes();
    } catch (IOException e) {
      throw new BadRequestException("Could not decompress GZIP data", e);
    }
  }

  public String peekContents() {
    if (isGzipped(contents)) {
      final StringBuilder buf = new StringBuilder(64).append("[gzip] ");
      final int maxLength = Math.min(contents.length, 16);
      for (int i = 0; i < maxLength; i++) {
        buf.append(String.format("%02x", contents[i]));
        if (i < maxLength - 1) {
          buf.append(',');
        }
      }
      if (contents.length > maxLength) {
        buf.append('\u2026'); // ellipsis
      }
      return buf.toString();
    }

    return abbreviateMiddle(contents);
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("sha224", sha224)
        .add("contents", peekContents())
        .toString();
  }
}
