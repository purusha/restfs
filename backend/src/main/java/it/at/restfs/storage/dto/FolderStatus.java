package it.at.restfs.storage.dto;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter  
@NoArgsConstructor 
@ToString 
@EqualsAndHashCode(callSuper = true)
public class FolderStatus extends FileStatus {
    
    private List<FileStatus> children;
      
    public void setChildren(List<FileStatus> children) {
        this.children = children;
        
        setLength((long) children.size());
    }
    
}
