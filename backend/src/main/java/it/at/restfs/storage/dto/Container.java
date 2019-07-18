package it.at.restfs.storage.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;

@Data
public class Container {

    private String name;
    
    private UUID id;
    
    //HttpStatusCode => NumberOf
    private Map<Integer, Long> statistics = new HashMap<>();
    
    private boolean statsEnabled = false;
    
    private boolean webHookEnabled = false;
    
}
