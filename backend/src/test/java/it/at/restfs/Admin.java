package it.at.restfs;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface Admin {

    @FormUrlEncoded
    @Headers("Accept: application/json")        
    @POST("{path}")
    Call<ResponseBody> create(
        @retrofit2.http.Path(value = "path") String path,
        @FieldMap Map<String, String> fields
    );
    
}
