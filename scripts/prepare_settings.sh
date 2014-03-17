#!/bin/sh
SONATYPE_PASSWORD=${SONATYPE_PASSWORD-}
GITHUB_PASSWORD=${GITHUB_PASSWORD-}
GITHUB_PASSWORD="$(echo ${GITHUB_PASSWORD} | base64 -d)"

# force repo rebuild
rm -rf ~/.m2

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
</settings>
" > ~/.m2/settings_deploy.xml
