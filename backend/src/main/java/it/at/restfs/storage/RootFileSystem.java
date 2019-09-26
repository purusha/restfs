package it.at.restfs.storage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.SystemUtils;

import com.google.common.io.Files;
import com.google.inject.Inject;

import it.at.restfs.http.services.PathHelper;
import lombok.Getter;
import lombok.SneakyThrows;

public class RootFileSystem {
	
	//XXX get value from System Property or Configuration File
	private static final String ROOT = "/tmp/" + PathHelper.APP_NAME + "/";
	
	@Getter	
	private final String root; 
	
	@Inject
	public RootFileSystem() {
		if (SystemUtils.IS_OS_WINDOWS) {
			root = createRootWin32();
		} else {
			root = createRootUnix();
		}
	}

	@SneakyThrows
	private String createRootUnix() {
		new File(ROOT).mkdirs();
		
		return ROOT;
	}

	private String createRootWin32() {
		return Files.createTempDir().getAbsolutePath() + File.separator;
	}	
	
	public boolean isUseful() {
		return Objects.nonNull(root);
	}
	
	public Path containerPath(UUID container, String path) {
		return Paths.get(root + File.separator + container + path);
	}
		
	public File fileOf(String prefix, UUID container) {
        return new File(root + File.separator + prefix + container);
    }
 
	public Path pathOf(String prefix, UUID container) {
        return Paths.get(root + File.separator + prefix + container);
    }	
}
