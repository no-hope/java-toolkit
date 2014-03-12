Known issues
============

At the moment this module is incompatible with protoc != 2.4.1. As a
workaround you may need to compile protoc 2.4.1 from source and set
path to protobuf compiler explicitly:

    mvn -DprotocExecutable=/path/to/2.4.1/protoc <targets>
