package it.at.restfs;

import java.util.Map;
import java.util.UUID;
import it.at.restfs.http.HTTPListener;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.QueryMap;

public interface RestFs {
    
    @Headers({
        "Accept: application/json",
    })    
    @POST("{path}?op=CREATE")
    Call<Void> create(
        @retrofit2.http.Path("path") String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,
        @QueryMap Map<String, String> options        
    );

    @Headers({
        "Accept: application/json",
    })        
    @POST("{path}?op=MKDIRS")
    Call<Void> mkdirs(
        @retrofit2.http.Path("path") String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,
        @QueryMap Map<String, String> options
    );

    @Headers({
        "Accept: application/json",
    })        
    @GET("{path}?op=GETSTATUS")
    Call<Void> getstatus(
        @retrofit2.http.Path("path") String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,
        @QueryMap Map<String, String> options
    );

    @Headers({
        "Accept: application/json",
    })        
    @GET("{path}?op=LISTSTATUS")
    Call<Void> liststatus(
        @retrofit2.http.Path("path") String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,
        @QueryMap Map<String, String> options
    );

    @Headers({
        "Accept: application/json",
    })        
    @PUT("{path}?op=RENAME")
    Call<Void> rename(
        @retrofit2.http.Path("path") String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,
        @QueryMap Map<String, String> options
    );

    @Headers({
        "Accept: application/json",
    })        
    @DELETE("{path}?op=DELETE")
    Call<Void> delete(
        @retrofit2.http.Path("path") String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,
        @QueryMap Map<String, String> options
    );
    
}
