package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;

public class TextRepoFile {

    private String sha1;
    private String name;
    private byte[] content;

    @ConstructorProperties({"sha1", "name", "content"})
    public TextRepoFile(
      String sha1,
      String name,
      byte[] content
    ) {
        this.sha1 = sha1;
        this.name = name;
        this.content = content;
    }

    @JsonProperty
    public String getSha1() {
        return sha1;
    }

    @JsonProperty
    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public byte[] getContent() {
        return content;
    }

    @JsonProperty
    public void setContent(byte[] content) {
        this.content = content;
    }
}