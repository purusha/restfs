package it.at.restfs.storage;

import java.util.UUID;

public interface Storage {

    void listStatus(UUID container, String path);

    void getStatus(UUID container, String path);

    void open(UUID container, String path);

}
