#!/bin/bash

set -e
set -e pipefail

POM_NAMESPACE="http://maven.apache.org/POM/4.0.0"

if [[ -f "pom.xml" ]]; then
    PROTOC_VERSION="$(xmlstarlet sel -N p=${POM_NAMESPACE} -t -v '/p:project/p:properties/p:protobuf.version/text()' pom.xml)"
fi

PROTOC_VERSION="${PROTOC_VERSION:-2.5.0}"
PROTOC_DISTR_NAME="protobuf-${PROTOC_VERSION}"
PROTOC_DISTR_FILE="${PROTOC_DISTR_NAME}.tar.gz"
PROTOC_PATH="/tmp/${PROTOC_DISTR_NAME}/src/protoc"

SONATYPE_PASSWORD=${SONATYPE_PASSWORD-}
GITHUB_PASSWORD=${GITHUB_PASSWORD-}
GITHUB_PASSWORD="$(echo ${GITHUB_PASSWORD} | base64 -d)"
