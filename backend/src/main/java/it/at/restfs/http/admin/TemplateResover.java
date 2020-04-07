package it.at.restfs.http.admin;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.google.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class TemplateResover {
    
    private final static ObjectMapper MAPPER = new ObjectMapper();
    
    //XXX NOT Recompile mode
    private final Map<String, Template> mapping = new HashMap<>();
    
    //XXX NOT Recompile mode
    private final Handlebars handlebars = handlebars();
    
    public Template get(String name) throws IOException {
        //XXX NOT Recompile mode            	
        return mapping.computeIfAbsent(name, s -> {
            try {
                return handlebars.compile(s);
            } catch (IOException e) {
                LOGGER.error("can't compile template {}", name, e.getMessage());
                return null;
            }
        });

        //XXX Recompile mode
        //return handlebars().compile(name); 
    }
    
    /*
        other helpers should be get from
        
        https://github.com/jknack/handlebars.java/tree/master/handlebars/src/main/java/com/github/jknack/handlebars/helper               
     */        
    private Handlebars handlebars() {
        final Handlebars handlebars = new Handlebars(new ClassPathTemplateLoader("/templates"));
        
        handlebars.registerHelper("size", new Helper<Object>() {
            @Override
            public CharSequence apply(Object ob, Options options) throws IOException {
                if (ob instanceof Collection || ob instanceof Map) {
                    try {
                        final Method method = ob.getClass().getMethod("size");
                        method.setAccessible(true); //HashMap.KeySet is not public !!?
                        
                        return String.valueOf(method.invoke(ob));
                    } catch (Exception e) {
                        LOGGER.error("", e);
                        return StringUtils.EMPTY;
                    }
                } else {
                    LOGGER.error("cannot access method 'size' on {} of class {}", ob, ob.getClass());
                    return StringUtils.EMPTY;
                }
            }
        });

        handlebars.registerHelper("json", (o, options) -> MAPPER.writeValueAsString(o));
        
        handlebars.registerHelper("eq", new Helper<Object>() {
            @Override
            public Object apply(Object a, Options options) throws IOException {
                final Object b = options.param(0, null);
                boolean result = new EqualsBuilder().append(a, b).isEquals();
                
                if (options.tagType == TagType.SECTION) {
                    return result ? options.fn() : options.inverse();
                } else {
                    return result ? options.hash("yes", true) : options.hash("no", false);                    
                }                                 
            }            
        });
        
        handlebars.registerHelper("toS", (o, options) -> {
            if (o instanceof Collection) {
                final Collection<?> c = (Collection<?>) o;
                return c.stream().map(e -> e.toString()).collect(Collectors.joining(", "));
            }
            
            return StringUtils.EMPTY;
        });

        handlebars.registerHelper("substring", (o, options) -> {
            final String str = o.toString();
            final Integer start = options.param(0);
            final Integer end = options.param(1, str.length());
            
            return str.subSequence(start, end);
        });  
        
        handlebars.registerHelper("sum", (o, options) -> {
            final Integer a = Integer.valueOf(o.toString());
            final Integer b = options.param(0);
            
            return  a + b;
        });

        return handlebars;
    }            
}    
