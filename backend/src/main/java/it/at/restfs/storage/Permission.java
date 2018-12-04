package it.at.restfs.storage;

import lombok.Data;

@Data
public class Permission {

    private char r;
    private char w;
    private char e;
}
