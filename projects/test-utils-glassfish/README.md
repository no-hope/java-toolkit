Wrappers for [Glassfish Embedded](http://docs.oracle.com/cd/E18930_01/html/821-2424/gjldt.html#scrolltoc),
suitable for complex integration testing.

Note that it may be required to increase heap/permgen size to work under Maven build:

    <plugin>
        <artifactId>maven-failsafe-plugin</artifactId>
        <configuration combine.self="append">
            <argLine>-XX:NewRatio=2 -server -XX:PermSize=64m -XX:MaxPermSize=192m -Xms256m -Xmx1024m</argLine>
            <forkMode>pertest</forkMode>
            <!-- ... -->
        </configuration>
    </plugin>

[See also](http://docs.oracle.com/cd/E19798-01/821-1754/gihus/index.html)


