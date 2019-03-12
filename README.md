# R Microservice
This is a simple REST microservice to process R documents.

# HTTP API
Analyses have to be POSTed first to the microservice before you can GET their results. It is assumed that the R script places its results in a "output" directory.
 
## Add document
To POST an analysis, create a JSON file which contains all necessary files. The files are described by their name (may contain a directory) and their Base64 encoded data.

We will upload two files:
```
$ cat main.R
people = read.csv2("input/people.csv", header = TRUE)
people$bmi <- people$mass/(people$size^2)
write.csv2(people, "output/people.csv", row.names = FALSE)

$ cat input/people.csv
id;name;mass;size
1;Alex;65;1,66
2;Bertha;74;1,69
3;Claude;55;1,57
4;Dorothea;86;1,73
```

Encoded with Base64, they look like this:
```
$ base64 -w0 main.R
XGRvY3VtZW50Y2xhc3N7YXJ0aWNsZX0KXGJlZ2lue2RvY3VtZW50fQpcaW5jbHVkZXt0ZXN0L3Rlc3R9ClxlbmR7ZG9jdW1lbnR9

$ base64 -w0 input/people.csv
aGFsbG8gd2VsdA==
```

We place their Base64 encoded content in a JSON (Base64 can just be used in JSON without doing any harm and no need to escape).
```
$ cat upload.json
{
  "files": [
     {
        "name": "main.R",
        "base64data": "cGVvcGxlID0gcmVhZC5jc3YyKCJpbnB1dC9wZW9wbGUuY3N2IiwgaGVhZGVyID0gVFJVRSkNCnBlb3BsZSRibWkgPC0gcGVvcGxlJG1hc3MvKHBlb3BsZSRzaXplXjIpDQp3cml0ZS5jc3YyKHBlb3BsZSwgIm91dHB1dC9wZW9wbGUuY3N2Iiwgcm93Lm5hbWVzID0gRkFMU0Up"
     },
     {
        "name": "input/people.csv",
        "base64data": "aWQ7bmFtZTttYXNzO3NpemUNCjE7QWxleDs2NTsxLDY2DQoyO0JlcnRoYTs3NDsxLDY5DQozO0NsYXVkZTs1NTsxLDU3DQo0O0Rvcm90aGVhOzg2OzEsNzMNCg=="
     }
  ]
}
```

Then we upload this JSON to the microservice. The POST request returns an unique ID which identifies the document.
```
$ curl -X POST -d @upload.json -H "Content-Type: application/json" -H "Accept: application/json" http://localhost/analyses/
2b5abff3-cf98-4837-b579-25bda3343bb9
```

## Get analysed data
When you GET the anylsis, you receive a JSON containing some information about the executed R process and all generated files. Those are again encoded in Base64.
```
$ curl -X GET -H "Content-Type: application/json" -H "Accept: application/json" http://localhost/analyses/2b5abff3-cf98-4837-b579-25bda3343bb9
{
  "success": true,
  "exitValue": 0,
  "durationMilliseconds": 17598,
  "files": [
    {
      "name": "output/results.csv",
      "base64data": "SSB3YXMgdG8gbGF6eSB0byBnZW5lcmF0ZSBhbiBhY3R1YWwgZXhhbXBsZS4gQnV0IG5pY2UgdGhhdCB5b3UgZGVjb2RlZCB0aGlzIQ=="
    }
  ],
  "output": "[... here might be some R output...]"
```

# Configuration
## main file
By default, the microservice will execute the uploaded `main.R` file. If a built-in R file should be executed, the configuration key `r.mainfile` (or environment variable `R_MAINFILE`) can be set.
```
$ cat configuration.properties
r.mainfile=/app/master.R
``` 
With Docker, this file (and further files) can be easily provided by extending the `r-microservice` docker image. (Also see the `examples` directory.)

# Security
Be aware that the default configuration allows to execute any R file, which may contain malicious code.

If the microservice is used to execute always the same R script, it might be a good idea to provide a static R file instead of uploading it (see configuration of `r.mainfile` above).