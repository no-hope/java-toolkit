#!/bin/bash

. ./env.sh

if [[ ! -f "${PROTOC_PATH}" ]]; then
    cd /tmp/
    wget "https://protobuf.googlecode.com/files/${PROTOC_DISTR_FILE}"
    tar -xzf "${PROTOC_DISTR_FILE}"
    cd "${PROTOC_DISTR_NAME}"
    ./configure
    make -j "$(nproc)"
fi
