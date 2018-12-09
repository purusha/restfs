package it.at.restfs.storage;

import lombok.Data;

@Data
public class OpenFile {
    
    private String path;
    private String content;
    
}
