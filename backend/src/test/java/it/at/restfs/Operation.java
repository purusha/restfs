package it.at.restfs;

import com.google.common.collect.Lists;

public enum Operation {
	
    GETSTATUS, LISTSTATUS, OPEN,                        //GET
    MOVE, RENAME,                                       //PUT
    MKDIRS, CREATE, APPEND,                             //POST
    DELETE,                                             //DELETE
    
    //Management call from here
    STATS,
    LAST_N_CALL,
    TOKEN;
    
    public static boolean isManagement(Operation o) {
    	return Lists.newArrayList(STATS, LAST_N_CALL, TOKEN).contains(o);
    }
}    
