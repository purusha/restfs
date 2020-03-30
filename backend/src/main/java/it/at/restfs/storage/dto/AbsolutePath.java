package it.at.restfs.storage.dto;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public final class AbsolutePath {
	
	public static final AbsolutePath EMPTY = AbsolutePath.of("");

    @Getter
    private final String path;
    
    private AbsolutePath(String path) {
        if (StringUtils.startsWith(path, "/")) {
            this.path = path;
        } else {
            this.path = "/" + path;
        }
    }
    
    @JsonCreator
    public static AbsolutePath of(@JsonProperty("path") String path) {
        return new AbsolutePath(path);
    }
    
}
