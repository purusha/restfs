package it.at.restfs.storage.dto;

import java.util.List;
import lombok.Data;

@Data
public class OpenFile {
    
    private String path;
    private List<String> content;
    
}
