package it.at.restfs.storage;

import lombok.Data;

@Data
public class Permission {

    private boolean read;
    private boolean write;
    private boolean execute;
}
