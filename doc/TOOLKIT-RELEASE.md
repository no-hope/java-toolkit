Toolkit release process
=======================

Maven settings
--------------

TBD

Known issues
------------

As github site deploy process is still unstable you need to pass `skip.site-deploy` property on release

    mvn -e -B release:perform -Darguments="-Dskip.site-deploy=true"
