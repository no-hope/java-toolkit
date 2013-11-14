#!/bin/bash
TRAVIS_PULL_REQUEST=${TRAVIS_PULL_REQUEST-false}
if [[ "${TRAVIS_PULL_REQUEST}" == "false" ]]; then
    mvn -q -e -s ~/.m2/settings_deploy.xml -Dskip.coveralls=true -Dskip.tests=true deploy
    # TODO: uncomment when site will be ready
    # mvn -q -e -s ~/.m2/settings_deploy.xml -Dskip.coveralls=true -Dskip.tests=true site-deploy
fi
