package it.at.restfs.storage;

import java.util.UUID;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import it.at.restfs.storage.dto.AbsolutePath;
import it.at.restfs.storage.dto.AssetType;
import it.at.restfs.storage.dto.FileStatus;
import it.at.restfs.storage.dto.FolderStatus;
import it.at.restfs.storage.dto.OpenFile;

/*

	There are 2 different ways of accessing HDFS over http.
	
	Using WebHDFS	
		http://<active-namenode-server>:<namenode-port>/webhdfs/v1/<file-path>?op=OPEN
	
	Using HttpFs	
		http://<hadoop-httpfs-server>:<httpfs-port>/webhdfs/v1/<file-path>?op=OPEN		
				
	WebHDFS:	
		Pros:		
		> Built-in with default Hadoop installation		
		> Efficient as load is streamed from each data node		
		Cons:		
		> Does not work if high availability is enabled on cluster, Active namenode needs to be specified to use webHdfs
		
	HttpFs	
		Pros:		
		> Works with HA enabled clusters.		
		Cons:		
		> Needs to be installed as additional service.
		> Impacts performance because data is streamed from single node.
		> Creates single point of failure

	Additional performance implications of webHDFS vs HttpFs
		https://www.linkedin.com/today/post/article/20140717115238-176301000-accessing-hdfs-using-the-webhdfs-rest-api-vs-httpfs
	
	WebHDFS vs HttpFs Major difference between WebHDFS and HttpFs: 
		WebHDFS needs access to all nodes of the cluster and when some data is read it is transmitted from that node directly, whereas in HttpFs, 
		a singe node will act similar to a "gateway" and will be a single point of data transfer to the client node. So, HttpFs could be choked 
		during a large file transfer but the good thing is that we are minimizing the footprint required to access HDFS.
		
 */

public class HdfsStorage implements Storage {
	
	public interface Factory extends StorageFactory<HdfsStorage> {
		HdfsStorage create(UUID container);		
	}	
	
	//private final UUID container;
	
	@Inject
	public HdfsStorage(@Assisted UUID container) {
		//this.container = container;
	}	

	@Override
	public FolderStatus listStatus(AbsolutePath path) {
		return null;
	}

	@Override
	public FileStatus getStatus(AbsolutePath path) {
		return null;
	}

	@Override
	public OpenFile open(AbsolutePath path) {
		return null;
	}

	@Override
	public void make(AbsolutePath path, AssetType folder) {
	}

	@Override
	public void append(AbsolutePath path, String body) {
	}

	@Override
	public void delete(AbsolutePath path) {
	}

	@Override
	public AssetType typeOf(AbsolutePath path) {
		return null;
	}

	@Override
	public String rename(AbsolutePath path, AbsolutePath target) {
		return null;
	}

	@Override
	public String move(AbsolutePath path, AbsolutePath target) {
		return null;
	}

}
