package it.at.restfs.http;

import java.util.UUID;
import lombok.Data;

@Data
public class ErrorResource {

    private final UUID logref;
    private final UUID container;
    private final String path;
    private final String message;
    
    /*
        XXX: build this from Exception instance
     */
    
}
