package it.at.restfs.storage;

public class ResouceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = -8240191991632533057L;
    
    public ResouceNotFoundException(String s) {
        super(s);
    }
}
