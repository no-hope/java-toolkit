This POM is designed for CI environment usage. This pom implies a set of plugins and properties which may be
useful for testing/reporting routines.

### Properties

| Property              | Description                         | Default value  |
| --------------------- |:-----------------------------------:| --------------:|
| skip.tests            | test skipping (implies skip.jacoco) | false          |
| skip.it.tests         | skips integration tests             | ${skip.tests}  |
| skip.jacoco           | skips coverage                      | ${skip.tests}  |
| skip.javadoc          | skips javadoc generation            | false          |
| skip.enforcer         | skips dependency versions checks    | false          |
| javadoc.quiet         | enables verbose javadoc output      | true           |
| javadoc.level         | degree of API doc detalization      | public         |
| maven.version.lower   | Minimal accepted maven version      | 3.0.4          |
| maven.version.upper   | Maximal accepted maven version      | **empty**     |

### Testing

Verification of integration tests is disabled by default. To enable it you may override either override `verify.it.tests` in
child pom.xml or pass it directly from command line:

    mvn -Dverify.it.tests=true clean verify
