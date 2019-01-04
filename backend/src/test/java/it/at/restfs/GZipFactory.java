package it.at.restfs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Converter.Factory;
import retrofit2.Retrofit;

public class GZipFactory {
    
    @Slf4j
    private static class GZip extends Factory {
        
        @Override
        public Converter<?, RequestBody> requestBodyConverter(
            Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit
        ) {
            
            LOGGER.info("requestBodyConverter: ");
            LOGGER.info("type {}", type);
            LOGGER.info("parameterAnnotations {}", Arrays.toString(parameterAnnotations));
            LOGGER.info("methodAnnotations {}", Arrays.toString(methodAnnotations));
            LOGGER.info("retrofit {}", retrofit);
            LOGGER.info("");
            
            return new GZipConverter();
        }        
        
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(
            Type type, Annotation[] annotations, Retrofit retrofit
        ) {

            LOGGER.info("responseBodyConverter: ");
            LOGGER.info("type {}", type);
            LOGGER.info("annotations {}", Arrays.toString(annotations));
            LOGGER.info("retrofit {}", retrofit);
            LOGGER.info("");
            
            return super.responseBodyConverter(type, annotations, retrofit);
        }
        
        @Override
        public Converter<?, String> stringConverter(
            Type type, Annotation[] annotations, Retrofit retrofit
        ) {

            LOGGER.info("stringConverter: ");
            LOGGER.info("type {}", type);
            LOGGER.info("annotations {}", Arrays.toString(annotations));
            LOGGER.info("retrofit {}", retrofit);
            LOGGER.info("");            
            
            return super.stringConverter(type, annotations, retrofit);
        }
    }
    
    @Slf4j
    private static class GZipConverter implements Converter<String, RequestBody> {
        //see https://stackoverflow.com/questions/26360858/okhttp-gzip-post-body
        
        @Override
        public RequestBody convert(String value) throws IOException {
            LOGGER.info("value {}", value);
            
            final ByteArrayOutputStream arr = new ByteArrayOutputStream();
            
            final OutputStream zipper = new GZIPOutputStream(arr);
            zipper.write(value.getBytes(StandardCharsets.UTF_8));
            zipper.close();            
            
            return RequestBody.create(MediaType.get("application/json"), arr.toByteArray());            
        }        
    }
    
    public static Factory create() {
        return new GZip();
    }

}
