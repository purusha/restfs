package it.at.restfs.storage;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter  
@RequiredArgsConstructor 
@ToString 
@EqualsAndHashCode(callSuper = true)
public class FolderStatus extends FileStatus {
    
    private List<FileStatus> children;
      
    public void setChildren(List<FileStatus> children) {
        this.children = children;
        
        setLength((long) children.size());
    }
    
}
