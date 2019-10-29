package it.at.restfs;

import static java.nio.charset.Charset.defaultCharset;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public interface OSFeatures {
  
    List<String> ls();
    
    List<String> curl();
    
    List<String> catchOutputOf(Process process) throws IOException;
    
    public static OSFeatures build() {      
        if (StringUtils.equals("Mac OS X", System.getProperty("os.name"))) {
            return new MacOsx();
        } else {
            return new Linux();
        }      
    }
    
}

class Linux implements OSFeatures {

    @Override
    public List<String> ls() {
        final List<String> result = new ArrayList<String>();
        
        result.add("/bin/ls");
        result.add("-R1X");
        
        return result;
    }
    
    @Override
    public List<String> curl() {
        final List<String> result = new ArrayList<String>();
        
        result.add("/usr/bin/curl");
        result.add("-v");
        result.add("-s");
        
        return result;
    }    

    @Override
    public List<String> catchOutputOf(Process process) throws IOException {
        return IOUtils.readLines(
            process.getInputStream(), defaultCharset()
        ).stream()                
            .skip(1)
            .collect(Collectors.toList());
    }
  
}

class MacOsx implements OSFeatures {

    @Override
    public List<String> ls() {
        final List<String> result = new ArrayList<String>();
        
        result.add("/bin/ls");
        result.add("-R1");        
        
        return result;
    }

    @Override
    public List<String> curl() {
        final List<String> result = new ArrayList<String>();
        
        result.add("/usr/bin/curl");
        result.add("-v");
        result.add("-s");
        
        return result;
    }    
    
    @Override
    public List<String> catchOutputOf(Process process) throws IOException {
        return IOUtils.readLines(
            process.getInputStream(), defaultCharset()
        );
    }
  
}

//XXX use and fill me please !!?
class Windows implements OSFeatures {

	@Override
	public List<String> ls() {
		return null;
	}

    @Override
    public List<String> curl() {
        return null;
    }    
	
	@Override
	public List<String> catchOutputOf(Process process) throws IOException {
		return null;
	}

}
