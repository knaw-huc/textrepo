package nl.knaw.huc.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import javax.annotation.Nonnull;
import java.beans.ConstructorProperties;
import java.nio.charset.StandardCharsets;

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

  @JsonProperty
  public String getSha224() {
    return sha224;
  }

  @JsonProperty
  public byte[] getContents() {
    return contents;
  }

  @JsonIgnore
  public String asUtf8String() {
    return new String(contents, StandardCharsets.UTF_8);
  }
}
