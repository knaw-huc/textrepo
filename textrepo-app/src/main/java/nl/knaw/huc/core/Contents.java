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
  private final byte[] content;

  public static Contents fromContent(@Nonnull byte[] content) {
    return new Contents(SHA_224.digestAsHex(content), content);
  }

  @ConstructorProperties({"sha224", "content"})
  public Contents(String sha224, byte[] content) {
    this.sha224 = sha224;
    this.content = content;
  }

  @JsonProperty
  public String getSha224() {
    return sha224;
  }

  @JsonProperty
  public byte[] getContent() {
    return content;
  }

  @JsonIgnore
  public String asUtf8String() {
    return new String(content, StandardCharsets.UTF_8);
  }
}
