# restfs
Restfs exposes your file system as a rest api. Is not a static file server, but instead lets you browse stats, and manipulate files through arbitrary paths.

Separation (each container have own ROOT folder)

Operation on container:
 - create folder (any path)
 - create file (any path)
 - get file attributes
 - get folder attributes
 - rename file
 - rename folder
 - delete file
 - delete folder
 - move file in any existing path
 - move folderX in any existing folder
 - append text on existing file (text or gzip)
 - retrieve file

Management operation on container:
 - stats
 - last N call (N configurable)
 - webhook (configurable for flush data on expired time or number of events)

This is project is under develop ... see todo file to discover new functionality
[todo file](./backend/todo)
