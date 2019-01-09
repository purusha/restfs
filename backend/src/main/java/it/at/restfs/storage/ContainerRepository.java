package it.at.restfs.storage;

import java.util.UUID;

public interface ContainerRepository {

    Container load(UUID container);

}
