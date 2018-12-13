package it.at.restfs;

import java.util.UUID;
import it.at.restfs.http.HTTPListener;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface RestFs {
    
    @POST("{path}?op=CREATE")
    Call<Void> create(
        @retrofit2.http.Path("path") String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,
        @Header("Accept") String accept
    );

    @POST("{path}?op=MKDIRS")
    Call<Void> mkdirs(
        @retrofit2.http.Path("path") String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,
        @Header("Accept") String accept
    );

    @GET("{path}?op=GETSTATUS")
    Call<Void> getstatus(
        @retrofit2.http.Path("path") String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,
        @Header("Accept") String accept
    );

    @GET("{path}?op=LISTSTATUS")
    Call<Void> liststatus(
        @retrofit2.http.Path("path") String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,
        @Header("Accept") String accept
    );
    
}
