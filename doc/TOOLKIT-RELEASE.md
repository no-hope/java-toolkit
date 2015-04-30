Toolkit release process
=======================

Maven settings
--------------

TBD

Known issues
------------

As github site deploy process is still unstable you need to pass `skip.site-deploy` property on release

    mvn -e -B release:perform -Dgpg.keyname=ID -Dgpg.passphrase=password -Darguments="-Dgpg.keyname=ID -Dgpg.passphrase=password -Dskip.site-deploy=true"
