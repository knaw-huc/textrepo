package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;

public interface FileService {
    void addFile(TextRepoFile file);

    TextRepoFile getBySha224(String sha224);
}
