package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;

public class TextRepoFile {

    private String sha224;
    private String name;
    private byte[] content;

    @ConstructorProperties({"sha224", "name", "content"})
    public TextRepoFile(
      String sha224,
      String name,
      byte[] content
    ) {
        this.sha224 = sha224;
        this.name = name;
        this.content = content;
    }

    @JsonProperty
    public String getSha224() {
        return sha224;
    }

    @JsonProperty
    public void setSha224(String sha224) {
        this.sha224 = sha224;
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