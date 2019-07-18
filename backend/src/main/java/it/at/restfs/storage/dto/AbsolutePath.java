package it.at.restfs.storage.dto;

import org.apache.commons.lang3.StringUtils;
import lombok.Getter;

public final class AbsolutePath {

    @Getter
    private final String path;
    
    private AbsolutePath(String path) {
        if (StringUtils.startsWith(path, "/")) {
            this.path = path;
        } else {
            this.path = "/" + path;
        }
    }
    
    public static AbsolutePath of(String path) {
        return new AbsolutePath(path);
    }
    
}
