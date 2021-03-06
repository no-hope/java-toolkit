# Apache Archiva OSGi Bundle Repository Plugin

This projects is based on [SolarNetwork/solarnetwork-build][5] and implements an [Apache Archiva][1] plugin that
produces an [OSGi Bundle Repository][2] metadata file based on the available OSGi bundles hosted in Archiva
repositories.

# Configuring

Copy the **archiva-obr-plugin-X.jar** you downloaded or built to the `WEB-INF/lib`
directory.

Next, modify the `conf/archiva.xml` file and look for a section starting with
`<knownContentConsumers>`. There will be a list of `<knownContentConsumer>` elements
in that section. Add another line after the last one like this:

    <knownContentConsumer>create-obr-metadata</knownContentConsumer>

Or you may go to `Repository Scanning -> Consumers` section and enable `create-obr-metadata` consumer.

Now you should be able to (re)start Archiva, and kick off a directory scan on your
desired repository to generate the OBR metadata file.

  [1]: http://archiva.apache.org/
  [2]: http://felix.apache.org/site/apache-felix-osgi-bundle-repository.html
  [3]: http://search.maven.org/#artifactdetails|org.apache.felix|org.apache.felix.bundlerepository|2.2.0|bundle
  [4]: http://search.maven.org/#artifactdetails|org.osgi|org.osgi.core|4.1.0|jar
  [5]: https://github.com/SolarNetwork/solarnetwork-build/tree/master/archiva-obr-plugin
