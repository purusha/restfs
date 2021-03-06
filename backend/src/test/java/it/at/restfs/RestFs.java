package it.at.restfs;

import java.util.Map;
import java.util.UUID;

import it.at.restfs.http.api.HTTPListener;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.QueryMap;

public interface RestFs {
    
    @Headers("Accept: application/json")    
    @POST("{path}?op=CREATE")
    Call<ResponseBody> create(
        @retrofit2.http.Path(value = "path", encoded = true) String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container        
    );

    @Headers("Accept: application/json")    
    @POST("{path}?op=MKDIRS")
    Call<ResponseBody> mkdirs(
        @retrofit2.http.Path(value = "path", encoded = true) String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container
    );

    @Headers("Accept: application/json")    
    @GET("{path}?op=GETSTATUS")
    Call<ResponseBody> getstatus(
        @retrofit2.http.Path(value = "path", encoded = true) String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container
    );

    @Headers("Accept: application/json")
    @GET("{path}?op=LISTSTATUS")
    Call<ResponseBody> liststatus(
        @retrofit2.http.Path(value = "path", encoded = true) String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container
    );

    @Headers("Accept: application/json")
    @PUT("{path}?op=RENAME")
    Call<ResponseBody> rename(
        @retrofit2.http.Path(value = "path", encoded = true) String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,
        @QueryMap Map<String, String> options
    );

    @Headers("Accept: application/json")
    @DELETE("{path}?op=DELETE")
    Call<ResponseBody> delete(
        @retrofit2.http.Path(value = "path", encoded = true) String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container
    );

    @Headers("Accept: application/json")
    @PUT("{path}?op=MOVE")
    Call<ResponseBody> move(
        @retrofit2.http.Path(value = "path", encoded = true) String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,
        @QueryMap Map<String, String> options
    );

    @Headers("Accept: application/json")        
    @POST("{path}?op=APPEND")
    Call<ResponseBody> append(
        @retrofit2.http.Path(value = "path", encoded = true) String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,        
        @Body String body        
    );
    
    @Headers("Accept: application/json")
    @GET("{path}?op=OPEN")
    Call<ResponseBody> open(
        @retrofit2.http.Path(value = "path", encoded = true) String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container
    );
    
    /*
     * 	Management Operation starts here
     */

    @Headers("Accept: application/json")
    @GET("stats")
    Call<ResponseBody> stats(
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container
    );

    @Headers("Accept: application/json")
    @GET("last")
    Call<ResponseBody> last(
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container
    );
    
    @Headers("Accept: application/json")
    @POST("token")
    Call<ResponseBody> token(
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container
    );    
    
}
