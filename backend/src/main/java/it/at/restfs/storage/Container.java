package it.at.restfs.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;

@Data
public class Container {

    private String name;
    
    private UUID id;
    
    //HttpStatusCode => NumberOf
    private Map<Integer, Integer> statistics = new HashMap<>(); 
    
}
