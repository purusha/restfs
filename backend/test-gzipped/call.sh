curl -v -s --trace-ascii http_trace.log --data-binary @file.gz -H "Accept: application/json" -H "Authorization: 42" -H "X-Container: 4a42ed08-2e32-45ee-8b4f-d1ef6135a181" -H "Content-Type: text/xml" -H "Content-Encoding: gzip" -X POST http://localhost:8081/restfs/v1/file?op=APPEND