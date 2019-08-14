package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import java.beans.ConstructorProperties;

public class TextRepoFile {
    private static final DigestUtils SHA_224 = new DigestUtils(MessageDigestAlgorithms.SHA_224);

    private final String sha224;
    private final byte[] content;

    public static TextRepoFile fromContent(byte[] content) {
        return new TextRepoFile(SHA_224.digestAsHex(content), content);
    }

    @ConstructorProperties({"sha224", "content"})
    public TextRepoFile(String sha224, byte[] content) {
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
}