# restfs
Restfs exposes your file system as a rest api. Is not a static file server, but instead lets you browse stats, and manipulate files through arbitrary paths.  

It is possible to manage multiple containers (multi tenant), each with its own "virtual" file system.  
For this reason the X-Container header is mandatory in every single request.  
Each container has its own method for authenticating requests; the available methods are:

1. oAuth
2. basic authentication
3. generate token from master password
4. no auth
  
Available operation on container:

 - create folder (any path)

	_Request:_  
	POST /dir/dir1/dir2?**op=MKDIRS**  
	Authorization: XXX-YYY-ZZZ  
	Accept: application/json  
	X-Container: UUU-OOO-PPP-KKK-LLL-HGF  
	  
	_Response:_  
	```json
    {
        "children":[
        ],
        "created":"2019-01-29@17:05:54",
        "lastAccess":"2019-01-29@17:05:54",
        "length":0,
        "modified":"2019-01-29@17:05:54",
        "name":"dir2",
        "type":"FOLDER"
    }
	```

 - create file (folder must exist)

	_Request:_  
	POST /dir/user42?**op=CREATE**  
	Authorization: XXX-YYY-ZZZ  
	Accept: application/json  
	X-Container: UUU-OOO-PPP-KKK-LLL-HGF  
	  
	_Response:_  
	```json
    {
        "created":"2019-01-29@17:05:54",
        "lastAccess":"2019-01-29@17:05:54",
        "length":0,
        "modified":"2019-01-29@17:05:54",
        "name":"user42",
        "type":"FILE"
    }
	```

 - get file attributes

	_Request:_  
	GET /dir/user42?**op=GETSTATUS**  
	Authorization: XXX-YYY-ZZZ  
	Accept: application/json  
	X-Container: UUU-OOO-PPP-KKK-LLL-HGF  
	  
	_Response:_  
	```json
    {
        "created":"2019-01-29@17:05:54",
        "lastAccess":"2019-01-29@17:05:54",
        "length":0,
        "modified":"2019-01-29@17:05:54",
        "name":"user42",
        "type":"FILE"
    }
	```

 - get folder attributes

	_Request:_  
	GET /dir/dir1?**op=LISTSTATUS**  
	Authorization: XXX-YYY-ZZZ  
	Accept: application/json  
	X-Container: UUU-OOO-PPP-KKK-LLL-HGF  
	  
	_Response:_  
	```json
    {
        "children":[
        ],
        "created":"2019-01-29@17:05:54",
        "lastAccess":"2019-01-29@17:05:54",
        "length":0,
        "modified":"2019-01-29@17:05:54",
        "name":"dir1",
        "type":"FOLDER"
    }
	```

 - rename file

	_Request:_  
	PUT /dir/dir2/user42?**op=RENAME**&**target=user42**  
	Authorization: XXX-YYY-ZZZ  
	Accept: application/json  
	X-Container: UUU-OOO-PPP-KKK-LLL-HGF  
	  
	_Response:_  
	```json
	{
	}
	```

 - rename folder

	_Request:_  
	PUT /dir/dir1?**op=RENAME**&**target=dir2**  
	Authorization: XXX-YYY-ZZZ  
	Accept: application/json  
	X-Container: UUU-OOO-PPP-KKK-LLL-HGF  
	  
	_Response:_  
	```json
	{
	}
	```

 - delete file

	_Request:_  
	DELETE /dir/email-reset42?**op=DELETE**  
	Authorization: XXX-YYY-ZZZ  
	Accept: application/json  
	X-Container: UUU-OOO-PPP-KKK-LLL-HGF  
	  
	_Response:_  
	```json
	{
	}
	```

 - delete folder

	_Request:_  
	DELETE /dir/to-be-deleted?**op=DELETE**  
	Authorization: XXX-YYY-ZZZ  
	Accept: application/json  
	X-Container: UUU-OOO-PPP-KKK-LLL-HGF  
	  
	_Response:_  
	```json
	{
	}
	```

 - move file in any existing path

	_Request:_  
	PUT /dir/dir2/user42?**op=MOVE**&**target=dir/dir2**  
	Authorization: XXX-YYY-ZZZ  
	Accept: application/json  
	X-Container: UUU-OOO-PPP-KKK-LLL-HGF  
	  
	_Response:_  
	```json
	{
	}
	```

 - move folderX in any existing folder

	_Request:_  
	PUT /dir/dir2/dir3/dir4?**op=MOVE**&**target=dir/dir2**  
	Authorization: XXX-YYY-ZZZ  
	Accept: application/json  
	X-Container: UUU-OOO-PPP-KKK-LLL-HGF  
	  
	_Response:_  
	```json
	{
	}
	```

 - append text on existing file (text or gzip)

	_Request:_  
	POST /dir/user42?**op=APPEND**  
	Authorization: XXX-YYY-ZZZ  
	Accept: application/json  
	X-Container: UUU-OOO-PPP-KKK-LLL-HGF  
	Content-Encoding: gzip OR identity (based on what add into the body)  
	BODY content is what will be saved into the file  
	  
	_Response:_  
	```json
    {
        "created":"2019-01-29@17:05:54",
        "lastAccess":"2019-01-29@17:05:54",
        "length":108,
        "modified":"2019-01-29@17:05:54",
        "name":"user42",
        "type":"FILE"
    }
	```

 - retrieve file

	_Request:_  
	GET /dir/user42?**op=OPEN**  
	Authorization: XXX-YYY-ZZZ  
	Accept: application/json  
	X-Container: UUU-OOO-PPP-KKK-LLL-HGF  
	  
	_Response:_  
	```json
    {
        "content":[
            "first line",
            "second line",
            "afsidbfasbdflasbdfljasdf asdfuasd fuas ydfasd fasudfyvasdf",
            "last line"
        ],
        "path":"/dir/file5"
    }
	```
  
Management operation on container:

 - stats

	_Request:_  
	GET /stats  
	Authorization: XXX-YYY-ZZZ  
	Accept: application/json  
	X-Container: UUU-OOO-PPP-KKK-LLL-HGF  
	  
	_Response:_  
	```json
	{
	}
	```
 
 - last N call (N configurable)

	_Request:_  
	GET /latest  
	Authorization: XXX-YYY-ZZZ  
	Accept: application/json  
	X-Container: UUU-OOO-PPP-KKK-LLL-HGF  
	  
	_Response:_  
	```json
	{
	}
	```
 
 - webhook (configurable for flush data on expired time or number of events)
 
This is project is under develop ... see todo file to discover new functionality
[todo file](./backend/todo)  

It is possible to build the project independently and run it on any machine;  
steps are:

1. git clone https://github.com/purusha/restfs.git
2. cd restfs/backend
3. mvn install
4. java -jar target/restfs-[VERSION].jar

 
Restfs expose two ports:
the first public http://localhost:8081 (to be used with an HTTP client); the second for administration purpose http://localhost:8086/containers (to be used with a browser) 

