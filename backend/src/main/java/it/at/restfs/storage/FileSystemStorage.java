package it.at.restfs.storage;

import java.util.UUID;
import it.at.restfs.http.HTTPListener;

public class FileSystemStorage implements Storage {
    
    private static final String ROOT = "/tmp/" + HTTPListener.APP_NAME + "/";

    @Override
    public void listStatus(UUID container, String path) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void getStatus(UUID container, String path) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void open(UUID container, String path) {
        // TODO Auto-generated method stub
        
    }

}
