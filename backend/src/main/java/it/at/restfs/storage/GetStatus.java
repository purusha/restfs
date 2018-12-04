package it.at.restfs.storage;

import java.util.Date;
import lombok.Data;

@Data
public class GetStatus {

//    private Path parent;
    private String name;
    private Date created;
    private Date modified;
    private Date lastAccess;
    private Permission permission;
    private Long length;
    private Boolean hidden;
    
}
