package it.at.restfs.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.at.restfs.storage.Storage;

@Singleton
public class PutController extends BaseController {

    @Inject
    public PutController(Storage storage) {
        super(storage);
    }

}
