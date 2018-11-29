package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.PathMatchers.*;

import java.util.function.Function;
import com.google.inject.Singleton;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.HTTPListener.Request;

@Singleton
public class DeleteController  implements Function<Request, Route> {

    @Override
    public Route apply(Request t) {
        return route();
    }
    
}
