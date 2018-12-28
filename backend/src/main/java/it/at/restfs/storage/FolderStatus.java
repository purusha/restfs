package it.at.restfs.storage;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter 
@Setter 
@RequiredArgsConstructor 
@ToString 
@EqualsAndHashCode(callSuper = true)
public class FolderStatus extends FileStatus {
    
    private List<FileStatus> files;
  
}
