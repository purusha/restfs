package it.at.restfs;

import com.google.inject.Guice;
import it.at.restfs.guice.AkkaModule;

public class StartSystem {
    public static void main(String[] args) {
        
        Guice
            .createInjector(
                new AkkaModule()
            )
            .getInstance(
                RestFsApplication.class
            )
            .run();
        
        
    }
}
