#!/bin/bash

source scripts/env.sh

# force repo rebuild (disabled for now)
#rm -rf ~/.m2/repository/
#rm ~/.m2/settings.xml
#[[ -d ~/.m2 ]] || mkdir ~/.m2

echo "
<settings>
    <servers>
        <server>
            <id>sonatype-nexus-snapshots</id>
            <username>ketoth.xupack</username>
            <password>${SONATYPE_PASSWORD}</password>
        </server>
        <server>
            <id>github</id>
            <username>ketoth.xupack@gmail.com</username>
            <password>${GITHUB_PASSWORD}</password>
        </server>
    </servers>
    <profiles>
        <profile>
            <id>protobuf-compiler</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <protocExecutable>${PROTOC_PATH}</protocExecutable>
            </properties>
        </profile>
    </profiles>
</settings>
" > ~/.m2/settings.xml
