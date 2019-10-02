package it.at.restfs;

public enum Operation {
	
    GETSTATUS, LISTSTATUS, OPEN,                        //GET
    MOVE, RENAME,                                       //PUT
    MKDIRS, CREATE, APPEND,                             //POST
    DELETE,                                             //DELETE
    
    //Management call from here
    STATS,
    LAST_N_CALL
}    
