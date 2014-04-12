#!/bin/bash

set -e
set -e pipefail

if [[ -f "pom.xml" ]]; then
    PROTOC_VERSION="$(xmlstarlet sel -t -v '//_:project//_:properties//_:protobuf.version/text()' pom.xml)"
fi

PROTOC_VERSION="${PROTOC_VERSION:-2.5.0}"
PROTOC_DISTR_NAME="protobuf-${PROTOC_VERSION}"
PROTOC_DISTR_FILE="${PROTOC_DISTR_NAME}.tar.gz"
PROTOC_PATH="/tmp/${PROTOC_DISTR_NAME}/src/protoc"

SONATYPE_PASSWORD=${SONATYPE_PASSWORD-}
GITHUB_PASSWORD=${GITHUB_PASSWORD-}
GITHUB_PASSWORD="$(echo ${GITHUB_PASSWORD} | base64 -d)"
