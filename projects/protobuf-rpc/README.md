Known issues
============

At the moment this module is incompatible with protoc != 2.4.1. As a
workaround you may need to compile protoc 2.4.1 from source and set
path to protobuf compiler explicitly:

    mvn -DprotocExecutable=/path/to/2.4.1/protoc <targets>

In release case you'll need to pass also this parameter in `arguments` parameter as follows:

    mvn release:<goal> -DprotocExecutable=<path> -Darguments=-DprotocExecutable=<path>
