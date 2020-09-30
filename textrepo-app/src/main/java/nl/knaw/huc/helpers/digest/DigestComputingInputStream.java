package nl.knaw.huc.helpers.digest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Nonnull;
import javax.ws.rs.BadRequestException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA_224;

/**
 * Compute (sha224) digest over the contents of an InputStream.
 * For large files, first reading all bytes then computing digest
 * over the consumed bytes becomes infeasible memory-wise.
 * This class computes digest on-the-fly.
 */
public class DigestComputingInputStream extends FilterInputStream {
  private final MessageDigest digest;

  public DigestComputingInputStream(InputStream in) {
    this(in, SHA_224);
  }

  protected DigestComputingInputStream(InputStream in, String algorithm) {
    super(in);
    this.digest = DigestUtils.getDigest(algorithm);
  }

  @Override
  public int read() {
    try {
      final int nread = in.read();
      if (nread > 0) {
        digest.update((byte) nread);
      }
      return nread;
    } catch (IOException e) {
      throw new BadRequestException("Could not compute digest of posted file (1)", e);
    }
  }

  @Override
  public int read(@Nonnull byte[] data, int off, int len) {
    try {
      final var nread = in.read(data, off, len);
      if (nread > 0) {
        digest.update(data, off, nread);
      }
      return nread;
    } catch (IOException e) {
      throw new BadRequestException("Could not compute digest of posted file (2)", e);
    }
  }

  public String digestAsHex() {
    return Hex.encodeHexString(digest.digest());
  }
}
